package me.flickersoul.autosafetree;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
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

        File maleStudentsAccounts = null;
        File malePasswords = null;
        File femalePasswords = null;
        File femaleStudentsAccounts = null;
        try {
            URI maleURL = new URI(maleAccount);
            maleStudentsAccounts = new File(maleURL);
        } catch (URISyntaxException e) {
            MainEntrance.logWarning("The Male Account Input Is Not A File");
        }

        try{
            URI femaleURI = new URI(femaleAccount);
            femaleStudentsAccounts = new File(femaleURI);
        } catch (URISyntaxException e) {
            MainEntrance.logWarning("The Female Account Input Is Not A File");
        }

        try{
            URI malePasswordURI = new URI(malePassword);
            malePasswords = new File(malePasswordURI);
        } catch (URISyntaxException e) {
            MainEntrance.logWarning("The Male Password Input Is Not A File");
        }

        try{
            URI femalePasswordURI = new URI(femalePassword);
            femalePasswords = new File(femalePasswordURI);

        } catch (URISyntaxException e) {
            MainEntrance.logWarning("The Female Password Input Is Not A File");
        }

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
    private String accountString;
    private File passwordFile;
    private String passwordString;
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

    public ThreadBootstrapperTemplate(File accountFile, String accountString, File passwordFile, String passwordString, boolean isMale, boolean renewFailedThreads) {
        this.accountFile = accountFile;
        this.accountString = accountString;
        this.passwordFile = passwordFile;
        this.passwordString = passwordString;
        this.isMale = isMale;
        this.renewFailedThreads = renewFailedThreads;
    }

    @Override
    public Boolean call() {
        Map<Future<Integer>, ProcessingThread> threadResultMap = new HashMap<>();
        ArrayList<String> accountList = new ArrayList<>();
        ArrayList<String> passwordList = new ArrayList<>();
        String universalPassword = ThreadBootstrapper.initPassword;

        if(accountFile == null){
            for (String subAccount : accountString.split(" ")) {
                accountList.add(subAccount);
            }
        } else {
            try (BufferedReader accountsReader = new BufferedReader(new FileReader(accountFile))) {
                for (String account; (account = accountsReader.readLine()) != null; ) {
                    accountList.add(account);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int linesInAccountFile = accountList.size();

        if(linesInAccountFile == 0){
            MainEntrance.logFatal("The Account Info Is Null; Please Recheck The File Before Going On");
            MainEntrance.logFatal("Exiting This Thread: " + this.toString());
            return false;
        }

        if(passwordFile == null) {
            String[] passwordArray =  passwordString.split(" ");
            if (passwordArray.length == 1){
                passwordList = null;
                universalPassword = passwordArray[0];
            } else {
                for (String subPassword : passwordArray) {
                    passwordList.add(subPassword);
                }

                int linesInPasswordFile = passwordList.size();

                if (linesInAccountFile > linesInPasswordFile) {
                    String lastPassword = passwordList.get(passwordList.size() - 1);

                    for (int i = 0; i < linesInAccountFile - linesInPasswordFile; i++) {
                        passwordList.add(lastPassword);
                    }
                }
            }
        } else {
            try (BufferedReader passwordReader = new BufferedReader(new FileReader(passwordFile))) {
                for (String password; (password = passwordReader.readLine()) != null; ) {
                    passwordList.add(password);
                }

                int linesInPasswordFile = passwordList.size();

                if (linesInPasswordFile == 1) {
                    passwordList = null;
                    universalPassword = passwordReader.readLine();
                } else if (linesInPasswordFile > 1 && linesInAccountFile > linesInPasswordFile) {
                    String lastPassword = passwordList.get(passwordList.size() - 1);
                    for (int i = 0; i < linesInAccountFile - linesInPasswordFile; i++) {
                        passwordList.add(lastPassword);
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
