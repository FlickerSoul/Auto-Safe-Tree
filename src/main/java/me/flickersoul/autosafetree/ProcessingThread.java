package me.flickersoul.autosafetree;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class ProcessingThread implements Callable<Integer> {
    private static final String HOME_PAGE = "https://longyan.xueanquan.com/";
    private static final String MY_HOME_WORK_PAGE = "https://longyan.xueanquan.com/JiaTing/JtMyHomeWork.html";

    private final static Pattern RADIO_PATTERN = Pattern.compile("//(\\w+)\\[@(\\w+)='(\\w+?)(No.|ABC)(\\w+?)(No.|ABC)'\\]");

    private static final String GLOBAL_CONFIG_KEY_FOR_TEST = "";

    private boolean isMaleStudent;
    private String accountName;
    private String password;
    private int questionTotalNumber;
    private WebClient webClient = new WebClient(BrowserVersion.CHROME);

    public static final int DONE = 0;
    public static final int LOGIN_FAILED = 1;
    public static final int INCOMPLETE_ERROR = 3;
    public static final int CANNOT_FIND_ELEMENT_ERROR_IN_SURVEY = 5;
    public static final int CANNOT_LOAD_NEXT_PAGE = 6;
    public static final int UNKNOWN_ERROR = 7;
    public static final int CANNOT_TYPE_IN = 8;
    public static final int INCORRECT_XPATH = 9;
    public static final int CANNOT_OPEN_SURVEY_PAGE = 10;
    public static final int CANNOT_OPEN_PAGE = 11;

    private static Logger logger = MainEntrance.getLogger();

    public ProcessingThread(String accountName, String password, boolean isMaleStudent){
        this.accountName = accountName;
        this.password = password;
        this.isMaleStudent = isMaleStudent;

        MainEntrance.logDebug("Start Running Auto Complete On Thread: " + this);

        initWebClient();
    }

    private void initWebClient(){
        logDebugWithOwner("Start Initiating Web Client...");
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setHistoryPageCacheLimit(0);
        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getCookieManager().setCookiesEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.waitForBackgroundJavaScript(60 * 1000);
        webClient.waitForBackgroundJavaScriptStartingBefore(10000);
        webClient.setAlertHandler((page, message) -> logErrorWithOwner("This page, " + page + ", has a message: \"" + message + "\"."));
        webClient.setHTMLParserListener(null);

        logDebugWithOwner("Finish Initiating Web Client");
    }

    @Override
    public Integer call() {
        int result = work();

        if(result == 0) {
            ThreadBootstrapperTemplate.currentTaskAdd();
            logInfoWithOwner("Closing Web Client...");
            webClient.close();
            System.gc();
            logInfoWithOwner("Web Client Closed");
        }

        return result;
    }

    private void logTraceWithOwner(String message){
        logger.trace(accountName + ": " + message);
    }

    private void logDebugWithOwner(String message){
        logger.debug(accountName + ": " + message);
    }

    private void logInfoWithOwner(String message){
        logger.info(accountName + ": " + message);
    }

    private void logWarningWithOwner(String message){
        logger.warn(accountName + ": " + message);
    }

    private void logErrorWithOwner(String message){
        logger.error(accountName + ": " + message);
    }

    private void logFatalWithOwner(String message){
        logger.fatal(accountName + ": " + message);
    }

    public void closeWebClient(){
        webClient.close();
    }

    public String toString(){
        return super.toString() + ": " + accountName + "; password: " + password + "; Is Male Student: " + isMaleStudent;
    }

    private int work() {
        logDebugWithOwner("Requesting Home Page...");
        HtmlPage rawPage = null;
        try {
            rawPage = webClient.getPage(HOME_PAGE);
        } catch (IOException e) {
            MainEntrance.logError(e.getMessage());
        }
        logInfoWithOwner("Got Home Page");

        logDebugWithOwner("Getting Required Element...");
        HtmlAnchor submitInput = rawPage.getHtmlElementById("LoginButton");
        HtmlTextInput account = rawPage.getHtmlElementById("UName");
        HtmlPasswordInput password = rawPage.getHtmlElementById("PassWord");
        logDebugWithOwner("Got Required Element For Login");

        logInfoWithOwner("Typing in Account Info...\nUser Name: " + accountName + "\nPassword: " + this.password);
        try {
            account.type(accountName);
            password.type(this.password);
        } catch (IOException e) {
            logFatalWithOwner(e.getMessage());
            logFatalWithOwner("Cannot Type In Account Name! Returning With Error Code: " + CANNOT_TYPE_IN);
            return CANNOT_TYPE_IN;
        }

        logDebugWithOwner("Type In Account Info Is Done");

        logInfoWithOwner("Login in...");
        HtmlPage loginPage = null;
        try {
            loginPage = submitInput.click();
            sleep(200);
        } catch (IOException e) {
            logFatalWithOwner("Cannot Click On Login Button");
            return LOGIN_FAILED;
        } catch (InterruptedException e) {
            logWarningWithOwner("The Sleep After Clicking stuCss Is Interrupted;");
        }


        try {
            logDebugWithOwner("Checking Login Status...");

            loginPage.getHtmlElementById("stuCss");

            logInfoWithOwner("Login Successfully");
        } catch (Exception e) {
            try {
                logWarningWithOwner("Cannot Find Login Status, Check Whether It's A New Account.");
                HtmlAnchor anchor = loginPage.getFirstByXPath("//a[@onclick='PwdClose()']");
                if (anchor != null) {
                    logInfoWithOwner("It's a new account, and its password should be reset. Skipping reset process...");
                    loginPage = anchor.click();
                }
                logDebugWithOwner("Skipped Resetting Password");
            } catch (NullPointerException e1) {
                logErrorWithOwner("The Password May Be Incorrect!");
            } catch (IOException e1) {
                logErrorWithOwner(e1.getMessage() + "\nUnknown Error");
            }

            try {
                loginPage.getHtmlElementById("stuCss");
                logInfoWithOwner("Login Successfully");
            } catch (Exception e1) {
                logErrorWithOwner("Cannot Find stuCss Element; Loging May Be Failed!");
            }
        }

        //Above is login

        logInfoWithOwner("Checking Status...");
        HtmlPage myHomeWorkPage;

        try {
            myHomeWorkPage = webClient.getPage(MY_HOME_WORK_PAGE);
        } catch (IOException e) {
            logFatalWithOwner("Cannot Get Into HomeWork Page; Returning With Error Code: " + CANNOT_OPEN_PAGE);
            return CANNOT_OPEN_PAGE;
        }

        logDebugWithOwner("Got Home Work Page");

        try {
            logInfoWithOwner("Waiting JavaScript Loading");
            sleep(1000);
        } catch (InterruptedException e) {
            logErrorWithOwner("Waiting Interrupted");
        }

        logDebugWithOwner("Getting Complete Status...");

        int incompleteTaskSize = myHomeWorkPage.getByXPath("//tr[@class='tr_wwc']").size();

        if (incompleteTaskSize == 0) {
            logInfoWithOwner("Did Not Detect Any Incomplete Work On Account! Exiting With Code: " + DONE);
            return DONE;
        } else if(incompleteTaskSize == myHomeWorkPage.getByXPath("//tbody/tr").size()){
            MainEntrance.logDebug("The Incomplete Task In Init Section Num Equals To Total Task Number; Try To Sleep Extra Time And Recheck");
            try {
                sleep(700);
                MainEntrance.logDebug("Slept Extra In Init Section Time");
            } catch (InterruptedException e) {
                MainEntrance.logWarning("Extra Sleep In Init Section Is Interrupted");
                MainEntrance.logDebug(e.getMessage());
            }

            if(myHomeWorkPage.getByXPath("//tr[@class='tr_wwc']").size() == 0){
                logInfoWithOwner("Did Not Detect Any Incomplete Work On Account! Exiting With Code: " + DONE);
                return DONE;
            }
        }

        try {
            if(myHomeWorkPage.getByXPath("//tbody[@id='mun_-1_1']").size() == 0){
                throw new Exception("Login Failed");
            }
        } catch (Exception e) {
            logFatalWithOwner(e.getMessage() + "\nLogin Failed! Exiting With Error Code: " + LOGIN_FAILED);
            return LOGIN_FAILED;
        }

        List<HtmlElement> testAnchors = myHomeWorkPage.getByXPath("//tr[@class='tr_wwc']/td[3][contains(text(), '安全学习')]/../td[2]/div/a");
        List<HtmlElement> surveyAnchors = myHomeWorkPage.getByXPath("//tr[@class='tr_wwc']/td[3][contains(text(), '专题活动')]/../td[2]/div/a");

        int testSize = testAnchors.size();
        int surveySize = surveyAnchors.size();

        logInfoWithOwner("You Have " + (testSize + surveySize) + " Task(s) Remaining Incomplete! Test(s): " + testSize + "; Survey(s): " + surveySize);

        logInfoWithOwner("Start Finishing Task(s)...");
        if (testAnchors.size() != 0) {
            logInfoWithOwner("Start Finishing Test(s)");
            for (HtmlElement subAnchor : testAnchors) {
                HtmlPage testPage = null;
                try {
                    testPage = webClient.getPage(subAnchor.click().getUrl());
                } catch (IOException e) {
                    logFatalWithOwner("Cannot Open Test Page!");
                    continue;
                }

                logInfoWithOwner("Open Test Page: " + testPage);

                TestMember testMember = MainWindowController.getItemFromTestConfigs(testPage.getUrl().toExternalForm());

                if (testMember == null)
                    testMember = MainWindowController.getItemFromTestConfigs(GLOBAL_CONFIG_KEY_FOR_TEST);

                if (testMember == null) {
                    logErrorWithOwner("Cannot Find The Configuration For This Test: " + testPage + "; Skipping");
                    continue;
                }

                logInfoWithOwner("Got Test Member: " + testMember);

                try {
                    logDebugWithOwner("Wait JS To Load...");
                    sleep(500);
                } catch (InterruptedException e) {
                    MainEntrance.logError(e.getMessage());
                }

                if (!testMember.getFirstButtonXPath().isBlank()) {
                    logInfoWithOwner("Detected First Button XPath In Config; Trying Clicking...");
                    try {
                        testPage = ((HtmlElement) testPage.getFirstByXPath(testMember.getFirstButtonXPath())).click();
                        logInfoWithOwner("Clicked First Button");
                    } catch (IOException e) {
                        logErrorWithOwner("Cannot Click On First Button On Page: " + testPage + "; This May Cause This Task Remain Incomplete");
                    }
                }

                logInfoWithOwner("Entering Test Section...");
                try {
                    HtmlElement stepTwoAnchor = testPage.getFirstByXPath(testMember.getSecondButtonXPath());
                    testPage = stepTwoAnchor.click();
                } catch (IOException e) {
                    logFatalWithOwner("Cannot Open Test Page: " + testPage + "!");
                } catch (NullPointerException e) {
                    logFatalWithOwner("Cannot Find Entrance Button For The Test On Page:  " + testPage + "!");
                    logDebugWithOwner(testPage.getWebResponse().getContentAsString());
                }

                logInfoWithOwner("Entered Test Section; Trying To Complete...");
                List<HtmlElement> elements = null;
                String selectorXPath = testMember.getSelectorXPath();

                if (selectorXPath.contains("@value")) {
                    logDebugWithOwner("The Selector Contains '@Value'");
                    try {
                        elements = testPage.getByXPath(selectorXPath);
                        logDebugWithOwner("Answers Are: " + elements);
                        for (HtmlElement element : elements) {
                            testPage = element.click();
                            logInfoWithOwner("Select The Right Answer In Question " + element);
                        }
                    } catch (NullPointerException e) {
                        logFatalWithOwner("Cannot find the element: " + elements + ".");
                        continue;
                    } catch (IOException e) {
                        logFatalWithOwner("Cannot click on the element: " + elements + ".");
                        continue;
                    }
                } else {
                    elements = testPage.getByXPath("");
                    //TODO 替换 second button XPATH
                    //TODO 暂时 throw， 增加问题答案输入，
                }


                HtmlButtonInput input = testPage.getFirstByXPath(testMember.getSubmitButtonXPath()); // [text()[contains(.,'Some text')]]
                try {
                    testPage = input.click();
                } catch (IOException e) {
                    logFatalWithOwner("Cannot Submit This Test: " + testPage + ". Returning With Error Code: " + UNKNOWN_ERROR);
                }
                logInfoWithOwner("Submit test: " + input + " : " + testPage.getUrl());
            }
        }

        if (surveySize != 0) {
            logInfoWithOwner("Start Completing Survey(s)");
            //开始

            for (SurveyMember member : MainWindowController.getSurveyMembers().values()) {
                HtmlPage surveyPage;
                String url = member.getPageUrl();
                try {
                    surveyPage = webClient.getPage(url);
                    logTraceWithOwner(surveyPage.getWebResponse().getContentAsString());
                } catch (IOException e) {
                    logFatalWithOwner("Cannot Open Survey Page: " + url + "; Returning With Error Code: " + CANNOT_OPEN_SURVEY_PAGE);
                    return CANNOT_OPEN_SURVEY_PAGE;
                }

                logInfoWithOwner("Open main Work Page: " + surveyPage);

                try {
                    logInfoWithOwner("Waiting JS"); //??????
                    sleep(500);
                } catch (InterruptedException e) {
                    MainEntrance.logError(e.getMessage());
                }

                //点击第一个观看完成框
                String firstXPath = member.getFirstButtonXPath();
                logDebugWithOwner("First Button XPath Is: " + firstXPath);
                HtmlElement stepOneDiv = surveyPage.getFirstByXPath(firstXPath);
                try {
                    logInfoWithOwner("Trying To Click On The First Button" + stepOneDiv);
                    stepOneDiv.click();
                    logInfoWithOwner("Clicked The \"Watched\" Button");
                } catch (IOException e) {
                    logErrorWithOwner("Cannot Click On First Button On Page " + surveyPage + "; This May Result In Incomplete Task");
                } catch (NullPointerException e) {
                    logErrorWithOwner("Cannot Click On First Button On Page " + surveyPage);
                }

                //开始完成调查
                HtmlElement stepTwoDiv = surveyPage.getFirstByXPath(member.getSecondButtonXPath());

                HtmlPage surveyTestPage;
                try {
                    surveyTestPage = stepTwoDiv.click();
                } catch (IOException e) {
                    logFatalWithOwner("Cannot Open Test In Survey Page: " + surveyPage + "; Returning With Error Code: " + CANNOT_OPEN_SURVEY_PAGE);
                    return CANNOT_OPEN_SURVEY_PAGE;
                }

                try {
                    logInfoWithOwner("Waiting JavaScript Loading");
                    sleep(500);
                } catch (InterruptedException e) {
                    logErrorWithOwner("Waiting Interrupted");
                }

                logInfoWithOwner("Click and open Survey Test Page: " + surveyTestPage);

                //id = radio_选项(0-n)_问题no(1-n)
                //id = radio_问题(1-n)_选项(1-n)
                char[] answerArray = isMaleStudent ? member.getMaleAnswerLine().toCharArray() : member.getFemaleAnswerLine().toCharArray();
                logInfoWithOwner("Answer For This Account Is " + answerArray);

                final int CLICK_INTERVAL = member.getClickInterval();

                final int NO_RECORD = member.getNoStartPoint();
                final int ABC_RECORD = member.getAbcStartPoint();

                Matcher matcher = RADIO_PATTERN.matcher(member.getSelectorXPath());
                // "//(\\w+)\\[@(\\w+)='(\\w+?)(No.|ABC)(\\w+?)(No.|ABC)'\\]"
                if (matcher.find()) {
                    final String RADIO = matcher.group(3);
                    final String LINK = matcher.group(5);

                    if (matcher.group(4).equals("No.")) {
                        for (int i = 0; i < answerArray.length; i++) {
                            int answerRecord = answerArray[i] - 'A' + ABC_RECORD;
                            int noRecord = i + NO_RECORD;
                            String questionId = new StringBuilder(RADIO).append(noRecord).append(LINK).append(answerRecord).toString();

                            try {
                                sleep(CLICK_INTERVAL);
                                surveyTestPage = surveyTestPage.getHtmlElementById(questionId).click();
                            } catch (Exception e) {
                                logErrorWithOwner("Cannot find the element with the id: " + questionId + "! Trying Turning To Next Page");
                                HtmlElement nextPage = surveyTestPage.getFirstByXPath(member.getNextPageXPath());

                                try {
                                    surveyTestPage = nextPage.click();
                                } catch (IOException e1) {
                                    logFatalWithOwner("Cannot Find The Element Using XPath: " + questionId + " After Loading Next Page. Returning With Error Code: " + CANNOT_LOAD_NEXT_PAGE);
                                    return CANNOT_LOAD_NEXT_PAGE;
                                }

                                try {
                                    surveyTestPage = surveyTestPage.getHtmlElementById(questionId).click();
                                } catch (IOException e1) {
                                    logFatalWithOwner("Cannot Click On The Element Using XPath: " + questionId + " After Loading Next Page. Returning With Error Code: " + CANNOT_FIND_ELEMENT_ERROR_IN_SURVEY);
                                    return CANNOT_FIND_ELEMENT_ERROR_IN_SURVEY;
                                }
                            }

                            logInfoWithOwner("click on " + questionId);
                        }
                    } else {
                        for (int i = 0; i < answerArray.length; i++) {
                            int answerRecord = answerArray[i] - 'A' + ABC_RECORD;
                            int noRecord = i + NO_RECORD;
                            String questionId = new StringBuilder(RADIO).append(answerRecord).append(LINK).append(noRecord).toString();

                            try {
                                sleep(CLICK_INTERVAL);
                                surveyTestPage = surveyTestPage.getHtmlElementById(questionId).click();
                            } catch (Exception e) {
                                logErrorWithOwner("Cannot find the element with the id: " + questionId + "! Trying Turning To Next Page");
                                HtmlElement nextPage = surveyTestPage.getFirstByXPath(member.getNextPageXPath());
                                try {
                                    surveyTestPage = nextPage.click();
                                } catch (IOException e1) {
                                    logFatalWithOwner("Cannot Find The Element Using XPath: " + questionId + " After Loading Next Page. Returning With Error Code: " + CANNOT_LOAD_NEXT_PAGE);
                                    return CANNOT_LOAD_NEXT_PAGE;
                                }

                                try {
                                    surveyTestPage = surveyTestPage.getHtmlElementById(questionId).click();
                                } catch (IOException e1) {
                                    logFatalWithOwner("Cannot Click On The Element Using XPath: " + questionId + " After Loading Next Page. Returning With Error Code: " + CANNOT_FIND_ELEMENT_ERROR_IN_SURVEY);
                                    return CANNOT_FIND_ELEMENT_ERROR_IN_SURVEY;
                                }
                            }

                            logInfoWithOwner("click on " + questionId);
                        }
                    }
                } else {
                    logFatalWithOwner("The XPath For Question Answers Is Incorrect; Exiting With Error Code: " + INCORRECT_XPATH);
                }

                HtmlElement input = surveyTestPage.getFirstByXPath(member.getSubmitButtonXPath());

                try {
                    surveyTestPage = input.click();
                } catch (IOException e) {
                    MainEntrance.logError(e.getMessage());
                }
                logInfoWithOwner("click on submit " + input);

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    MainEntrance.logError(e.getMessage());
                }
                logInfoWithOwner("Complete: " + surveyTestPage);
            }
        }

        logInfoWithOwner("Checking Status...");

        try {
            myHomeWorkPage = webClient.getPage(MY_HOME_WORK_PAGE);

            sleep(1000);
        } catch (IOException e) {
            MainEntrance.logError(e.getMessage());
        } catch (InterruptedException e) {
            MainEntrance.logError(e.getMessage());
        }

        int incompleteTaskNum = myHomeWorkPage.getByXPath("//tr[@class='tr_wwc']").size();

        if (incompleteTaskNum == 0) {
            MainEntrance.logInfo("\n===================\n\"" + accountName + "\" has completed the Test(s)!\nBy FlickerSoul\n===================");
            return DONE;
        } else if(incompleteTaskNum == myHomeWorkPage.getByXPath("//tbody/tr").size()){
            MainEntrance.logDebug("The Incomplete Task In Finalizing Section Num Equals To Total Task Number; Try To Sleep Extra Time And Recheck");
            try {
                sleep(700);
                MainEntrance.logDebug("Slept Extra Time In Finalizing Section");
            } catch (InterruptedException e) {
                MainEntrance.logWarning("Extra Sleep In Finalizing Section Is Interrupted");
                MainEntrance.logDebug(e.getMessage());
            }

            int remainingTaskSize = myHomeWorkPage.getByXPath("//tr[@class='tr_wwc']").size();
            if (remainingTaskSize == 0) {
                MainEntrance.logInfo("\n===================\n\"" + accountName + "\" has completed the Test(s)!\nBy FlickerSoul\n===================");
                return DONE;
            } else {
                logErrorWithOwner("There Is (Are) " + remainingTaskSize + " Work(s) Remaining Incomplete! Returning With Error Code: " + INCOMPLETE_ERROR);
                return INCOMPLETE_ERROR;
            }
        } else {
            logErrorWithOwner("There Is (Are) " + incompleteTaskNum + " Work(s) Remaining Incomplete! Returning With Error Code: " + INCOMPLETE_ERROR);
            return INCOMPLETE_ERROR;
        }
    }

}
