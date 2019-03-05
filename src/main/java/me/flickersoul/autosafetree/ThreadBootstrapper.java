package me.flickersoul.autosafetree;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ThreadBootstrapper{
    private static final ExecutorService closeListenerThreadPool = Executors.newSingleThreadExecutor(callable -> new Thread(callable, "Close Listenner Thread"));
    private static final ExecutorService bootstrapperThreadsPool = Executors.newFixedThreadPool(2, callable -> new Thread(callable, "Processing Thread Bootstrap Thread"));
    private static Future<Boolean> closeFuture;

    private static int threadCount;
    public static final String initPassword = "123456";
    private static final HashMap<ExecutorService, Future<Boolean>> templateThreadList = new HashMap<>();

    public synchronized static void init(String maleAccount, String malePassword, String femaleAccount, String femalePassword, int threadNum, boolean renewFailedThreads){
        templateThreadList.clear();
        if(closeFuture != null && (closeFuture.isDone() || closeFuture.isCancelled()))
            closeFuture.cancel(true);

        File maleStudentsAccounts = new File(maleAccount);
        File femaleStudentsAccounts = new File(femaleAccount);

        File malePasswords = new File(malePassword);
        File femalePasswords = new File(femalePassword);

        threadCount = threadNum / 2;
        MainEntrance.logDebug("Single Pool Thread Count: " + threadCount);

        ThreadBootstrapperTemplate maleTemplate = new ThreadBootstrapperForMaleStudents(maleStudentsAccounts, malePasswords, renewFailedThreads);

        Future<Boolean> maleFuture = bootstrapperThreadsPool.submit(maleTemplate);

        templateThreadList.put(maleTemplate.getPool(), maleFuture);
        MainEntrance.logDebug("Start Male Students' Processing Thread");

        ThreadBootstrapperTemplate femaleTemplate = new ThreadBootstrapperForFemaleStudents(femaleStudentsAccounts, femalePasswords, renewFailedThreads);
        Future<Boolean> femaleFuture = bootstrapperThreadsPool.submit(femaleTemplate);

        templateThreadList.put(femaleTemplate.getPool(), femaleFuture);

        MainEntrance.logDebug("Start Female Students' Processing Thread");

        invokeCloseListener();
    }

    private static void invokeCloseListener(){
        closeFuture = closeListenerThreadPool.submit(new CloseListener());
    }

    public static void terminateAllThread(){
        for(ExecutorService service : templateThreadList.keySet()){
            service.shutdownNow();
        }

        templateThreadList.clear();
    }

    public static void getAllFuture(){
        for(Future<Boolean> future : templateThreadList.values()){
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        templateThreadList.clear();
    }

    public static int getThreadCount(){
        return threadCount;
    }

    public static void shutdownBootstrapperThreadsPools(){
        bootstrapperThreadsPool.shutdownNow();
        closeListenerThreadPool.shutdownNow();
    }

}

class CloseListener implements Callable<Boolean> {

    @Override
    public Boolean call() {
        ThreadBootstrapper.getAllFuture();

        MainWindowController.switchWorkingStatus(false);

        return true;
    }
}

class ThreadBootstrapperTemplate implements Callable<Boolean>{
    private File accountFile;
    private File passwordFile;
    private final boolean isMale;
    private final boolean renewFailedThreads;
    private final ExecutorService pool = Executors.newFixedThreadPool(ThreadBootstrapper.getThreadCount(), runnable -> new Thread(runnable, "Processing Thread"));

    public ThreadBootstrapperTemplate(File accountFile, File passwordFile, boolean isMale, boolean renewFailedThreads) {
        this.accountFile = accountFile;
        this.passwordFile = passwordFile;
        this.isMale = isMale;
        this.renewFailedThreads = renewFailedThreads;
    }

    public ThreadBootstrapperTemplate(File accountFile, boolean isMale, boolean renewFailedThreads) {
        this.accountFile = accountFile;
        this.isMale = isMale;
        this.renewFailedThreads = renewFailedThreads;
    }

    @Override
    public Boolean call() {
        Map<Future<Integer>, ProcessingThread> threadResultMap = new HashMap<>();
        ArrayList<String> accountList = new ArrayList<>();
        ArrayList<String> passwordList = null;
        String universalPassword = ThreadBootstrapper.initPassword;


        try(BufferedReader accountsReader = new BufferedReader(new FileReader(accountFile))){
            for(String account; (account = accountsReader.readLine()) != null; ){
                accountList.add(account);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(passwordFile.canRead()) {
            try (BufferedReader passwordReader = new BufferedReader(new FileReader(passwordFile))) {
                final int linesInPasswordFile = countLine(passwordFile);

                if (linesInPasswordFile == 1) {
                    universalPassword = passwordReader.readLine();
                } else if (linesInPasswordFile > 1) {
                    passwordList = new ArrayList<>();
                    for (String password; (password = passwordReader.readLine()) != null; ) {
                        passwordList.add(password);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(passwordList == null || passwordList.size() != accountList.size()){
            for(String account : accountList){
                ProcessingThread thread = new ProcessingThread(account, universalPassword, isMale);
                threadResultMap.put(pool.submit(thread), thread);
                MainEntrance.logDebug("Add Thread: " + thread);
            }
        } else {
            for (int i = 0; i < passwordList.size(); i++) {
                ProcessingThread thread = new ProcessingThread(accountList.get(i), passwordList.get(i), isMale);
                threadResultMap.put(pool.submit(thread), thread);
                MainEntrance.logDebug("Add Thread: " + thread);
            }
        }

        MainEntrance.logDebug("Add All Thread");
        MainEntrance.logDebug("Ready To Invoke All Task");


        try {
            MainEntrance.logDebug("Got All Results");

            for(Iterator<Map.Entry<Future<Integer>, ProcessingThread>> resultIterator = threadResultMap.entrySet().iterator(); resultIterator.hasNext(); ) {
                Map.Entry<Future<Integer>, ProcessingThread> item = resultIterator.next();

                Future<Integer> resultKey = item.getKey();
                ProcessingThread resultValue = item.getValue();
                int resultCode = resultKey.get();

                MainEntrance.logDebug("Thread: " + resultValue + " Has Result Code: " + resultCode);
                if(resultCode == 0){
                    resultIterator.remove();
                } else {
                    resultIterator.remove();
                    threadResultMap.put(pool.submit(resultValue), resultValue);
                }
            }
            MainEntrance.logDebug("All Results Outputted");
        } catch (InterruptedException e) {
            MainEntrance.logWarning("The Batch Executions Are Interrupted");
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        MainEntrance.logInfo("Failed Task(s): " + threadResultMap.size());

        if(renewFailedThreads ){
            MainEntrance.logInfo("Renew Failed Task Is Turned On");
            if(threadResultMap.size() > 0) {
                MainEntrance.logInfo("; Retrying");
                try {
                    MainEntrance.logDebug("Got Renew Results");
                    for(Iterator<Map.Entry<Future<Integer>, ProcessingThread>> resultIterator = threadResultMap.entrySet().iterator(); resultIterator.hasNext(); ) {
                        Map.Entry<Future<Integer>, ProcessingThread> item = resultIterator.next();

                        ProcessingThread thread = item.getValue();
                        Future<Integer> resultKey = item.getKey();
                        int resultCode = resultKey.get();

                        MainEntrance.logDebug("Thread: " + thread + " Has Result Code: " + resultCode);
                        if (resultCode == 0) {
                            resultIterator.remove();
                        } else
                            thread.closeWebClient();
                    }
                    MainEntrance.logDebug("All Results Outputted");
                    MainEntrance.logWarning("Failed Results: " + threadResultMap.size());
                } catch (InterruptedException e) {
                    MainEntrance.logWarning("The Batch Executions Are Interrupted");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        MainEntrance.logDebug("Ready To Shut Down All The Pool Of This Thread: " + this);
        pool.shutdownNow();
        MainEntrance.logInfo("Terminate The Thread Pool");

        MainEntrance.logDebug("Ready To Clear All The Threads And Memory Caches");
        threadResultMap.clear();
        accountList.clear();
        if(passwordList != null)
            passwordList.clear();
        MainEntrance.logDebug("Cleared All The Threads And Memory Caches");

        MainEntrance.logDebug("Ready To Perform Garbage Collect");
        System.gc();
        MainEntrance.logDebug("Performed Garbage Collect");

        return true;
    }

    private int countLine(File file){
        int count = 0;
        boolean empty = true;

        try(InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] c = new byte[1024];
            int readChars = 0;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public ExecutorService getPool(){
        return pool;
    }
}

class ThreadBootstrapperForMaleStudents extends ThreadBootstrapperTemplate {
    public ThreadBootstrapperForMaleStudents(File accountFile, File passwordFile, boolean renewable) {
        super(accountFile, passwordFile, true, renewable);
    }
}

class ThreadBootstrapperForFemaleStudents extends ThreadBootstrapperTemplate {

    public ThreadBootstrapperForFemaleStudents(File accountFile, File passwordFile, boolean renewable) {
        super(accountFile, passwordFile, false, renewable);
    }
}
