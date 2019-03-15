package me.flickersoul.autosafetree;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ThreadBootstrapper{
    private static final ExecutorService closeListenerThreadPool = Executors.newSingleThreadExecutor(callable -> new Thread(callable, "Close Listenner Thread"));
    private static final ExecutorService bootstrapperThreadsPool = Executors.newFixedThreadPool(2, callable -> new Thread(callable, "Processing Thread Bootstrap Thread"));
    private static Future<Boolean> closeFuture;

    private static int threadCount;
    private static final HashMap<ExecutorService, Future<Boolean>> templateThreadList = new HashMap<>();

    public synchronized static void init(String maleAccount, String malePassword, String femaleAccount, String femalePassword, int threadNum, boolean renewFailedThreads){
        templateThreadList.clear();
        if(closeFuture != null && (closeFuture.isDone() || closeFuture.isCancelled()))
            closeFuture.cancel(true);

        MainEntrance.logDebug("Male Account String: " + maleAccount + "; Male Password String: " + malePassword + "; Female Account String: " + femaleAccount + "; Female Password String: " + femalePassword + "; Thread Number: " + threadNum + "; Renew?: " + renewFailedThreads);

        File maleStudentsAccounts;
        File malePasswords = null;
        File femalePasswords = null;
        File femaleStudentsAccounts;


        maleStudentsAccounts = new File(maleAccount);
        if(!maleStudentsAccounts.canRead()){
            MainEntrance.logWarning("The Male Account Input Is Not A File");
            maleStudentsAccounts = null;
        }

        femaleStudentsAccounts = new File(femaleAccount);
        if(!femaleStudentsAccounts.canRead()){
            MainEntrance.logWarning("The Female Account Input Is Not A File");
            femaleStudentsAccounts = null;
        }

        if(malePassword.isBlank()){
            MainEntrance.logWarning("The Male Password Input Is Empty");
        } else {
            malePasswords = new File(malePassword);
            if(!malePasswords.canRead()){
                MainEntrance.logWarning("The Male Password Input Is Not A File");
                malePasswords = null;
            }
        }

        if(femalePassword.isBlank()){
            MainEntrance.logWarning("The Female Password Input Is Empty");
        } else {
            femalePasswords = new File(femalePassword);
            if(!femalePasswords.canRead()){
                MainEntrance.logWarning("The Female Password Input Is Not A File");
                femalePasswords = null;
            }
        }

        threadCount = threadNum / 2;
        MainEntrance.logDebug("Single Pool Thread Count: " + threadCount);

        ThreadBootstrapperTemplate maleTemplate = new ThreadBootstrapperForMaleStudents(maleStudentsAccounts, maleAccount, malePasswords, malePassword, renewFailedThreads);

        Future<Boolean> maleFuture = bootstrapperThreadsPool.submit(maleTemplate);

        templateThreadList.put(maleTemplate.getPool(), maleFuture);
        MainEntrance.logDebug("Start Male Students' Processing Thread");

        ThreadBootstrapperTemplate femaleTemplate = new ThreadBootstrapperForFemaleStudents(femaleStudentsAccounts, femaleAccount, femalePasswords, femalePassword, renewFailedThreads);
        Future<Boolean> femaleFuture = bootstrapperThreadsPool.submit(femaleTemplate);

        templateThreadList.put(femaleTemplate.getPool(), femaleFuture);

        MainEntrance.logDebug("Start Female Students' Processing Thread");

        invokeCloseListener();
    }

    private static void invokeCloseListener(){
        closeFuture = closeListenerThreadPool.submit(new CloseListener());
    }

    public static void terminateAllThread(){
        for(Map.Entry<ExecutorService, Future<Boolean>> entry : templateThreadList.entrySet()){
            entry.getKey().shutdownNow();
            entry.getValue().cancel(true);
        }

        templateThreadList.clear();
    }

    public static void getAllFuture(){
        for(Future<Boolean> future : templateThreadList.values()){
            try {
                boolean result = future.get();
                if(result){
                    MainEntrance.logInfo("Finished One Template Without Errors");
                } else {
                    MainEntrance.logError("Something Wrong During Working On This Template; Please Click On Report Button To Send A Message To The Maintainer");
                }
            } catch (InterruptedException e) {
                MainEntrance.logError(e.getMessage());
            } catch (ExecutionException e) {
                MainEntrance.logError(e.getMessage());
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
    private static int totalTaskNum;
    private static Integer currentTask = 0;

    private File accountFile;
    private String accountString;
    private File passwordFile;
    private String passwordString;
    private final boolean isMale;
    private final boolean renewFailedThreads;
    private final ExecutorService pool = Executors.newFixedThreadPool(ThreadBootstrapper.getThreadCount(), runnable -> new Thread(runnable, "Processing Thread"));
    public static final String initPassword = "123456";

    public ThreadBootstrapperTemplate(File accountFile, String accountString, File passwordFile, String passwordString, boolean isMale, boolean renewFailedThreads) {
        this.accountFile = accountFile;
        this.accountString = accountString;
        this.passwordFile = passwordFile;
        this.passwordString = passwordString;
        this.isMale = isMale;
        this.renewFailedThreads = renewFailedThreads;

        MainEntrance.logDebug("Male: " + isMale + "; Account File: " + accountFile + "; Password File: " + passwordFile + "; Account String: " + accountString + "; Password String: " + passwordString);
    }

    @Override
    public Boolean call() {
        Map<Future<Integer>, ProcessingThread> threadResultMap = new HashMap<>();
        ArrayList<String> accountList = new ArrayList<>();
        ArrayList<String> passwordList = new ArrayList<>();
        String universalPassword = initPassword;

        if(accountFile == null){
            if(accountString.isBlank()){
                MainEntrance.logInfo("Empty Account Info; Exiting...");
                return true;
            }
            accountList.addAll(Arrays.asList(accountString.split(" ")));
        } else {
            try (BufferedReader accountsReader = new BufferedReader(new FileReader(accountFile))) {
                for (String account; (account = accountsReader.readLine()) != null; ) {
                    if(account.isBlank())
                        continue;
                    accountList.add(account.trim());
                }
            } catch (FileNotFoundException e) {
                MainEntrance.logError(e.getMessage());
            } catch (IOException e) {
                MainEntrance.logError(e.getMessage());
            }
        }

        int linesInAccountFile = accountList.size();

        if(linesInAccountFile == 0){
            MainEntrance.logFatal("The Account Info Is Null; Please Recheck Your Input Before Going On");
            MainEntrance.logFatal("Exiting This Thread: " + this.toString());
            return false;
        }

        totalTaskNum += linesInAccountFile;
        MainEntrance.logInfo("Total Task Num Now Is " + totalTaskNum);

        if(passwordFile == null) {
            if(passwordString.isBlank()){
                MainEntrance.logDebug("The Password String Is Blank; Setting Password List To Null");
                passwordList = null;
            } else
                passwordList.addAll(Arrays.asList(passwordString.split(" ")));
        } else {
            try (BufferedReader passwordReader = new BufferedReader(new FileReader(passwordFile))) {
                for (String password; (password = passwordReader.readLine()) != null; ) {
                    if(password.isBlank())
                        passwordList.add(initPassword);
                    passwordList.add(password);
                }

                if(passwordList.size() == 0){
                    MainEntrance.logFatal("The Password Info Is Null; Please Recheck Your Input Before Going On");
                    MainEntrance.logFatal("Exiting This Thread: " + this.toString());
                    return false;
                }
            } catch (FileNotFoundException e) {
                MainEntrance.logError(e.getMessage());
            } catch (IOException e) {
                MainEntrance.logError(e.getMessage());
            }
        }

        int linesInPasswordFile = 0;
        if(passwordList != null){
            linesInPasswordFile = passwordList.size();
        }

        if (linesInPasswordFile == 1) {
            MainEntrance.logDebug("Only One Password Is In The List; Setting Password List To Null And Changing Universal Password");
            universalPassword = passwordList.get(0);
            passwordList = null;
        } else if (linesInPasswordFile > 1 && linesInAccountFile > linesInPasswordFile) {
            String lastPassword = passwordList.get(passwordList.size() - 1);
            for (int i = 0; i < linesInAccountFile - linesInPasswordFile; i++) {
                passwordList.add(lastPassword);
            }
        }

        if(passwordList == null){
            MainEntrance.logDebug("Password List Is Null; Using Universal Password");
            for(String account : accountList){
                ProcessingThread thread = new ProcessingThread(account, universalPassword, isMale);
                threadResultMap.put(pool.submit(thread), thread);
                MainEntrance.logDebug("Add Thread: " + thread);
            }
        } else {
            MainEntrance.logDebug("Password List Is Not Null; Using Passwords Inside Of The List");
            for (int i = 0; i < passwordList.size(); i++) {
                ProcessingThread thread = new ProcessingThread(accountList.get(i), passwordList.get(i), isMale);
                threadResultMap.put(pool.submit(thread), thread);
                MainEntrance.logDebug("Add Thread: " + thread);
            }
        }

        MainEntrance.logDebug("Added All Thread! Ready To Invoke All Task!");

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
            MainEntrance.logDebug(e.getMessage());
        } catch (ExecutionException e) {
            MainEntrance.logError(e.getMessage());
        }

        MainEntrance.logInfo("Failed Task(s): " + threadResultMap.size());

        if(renewFailedThreads ){
            MainEntrance.logInfo("Renew Failed Task Is Turned On");
            if(threadResultMap.size() > 0) {
                MainEntrance.logInfo("Retrying");
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
                    MainEntrance.logDebug(e.getMessage());
                } catch (ExecutionException e) {
                    MainEntrance.logError(e.getMessage());
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

    public static void clearTaskCount(){
        totalTaskNum = 0;
        currentTask = 0;
        MainEntrance.logDebug("Cleared Task Count");
    }

    public synchronized static void currentTaskAdd(){
        currentTask += 1;
        MainWindowController.setProgressBarRation(currentTask/(double)totalTaskNum);
    }
}

class ThreadBootstrapperForMaleStudents extends ThreadBootstrapperTemplate {
    public ThreadBootstrapperForMaleStudents(File accountFile, String accountString, File passwordFile, String passwordString, boolean renewable) {
        super(accountFile, accountString, passwordFile, passwordString, true, renewable);
    }
}

class ThreadBootstrapperForFemaleStudents extends ThreadBootstrapperTemplate {

    public ThreadBootstrapperForFemaleStudents(File accountFile, String accountString, File passwordFile, String passwordString, boolean renewable) {
        super(accountFile, accountString, passwordFile, passwordString, false, renewable);
    }
}
