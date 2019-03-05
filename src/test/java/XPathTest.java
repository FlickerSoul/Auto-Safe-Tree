import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import me.flickersoul.autosafetree.FileChooserDriver;

import java.io.*;
import java.util.List;
import java.util.logging.Level;

public class XPathTest {

    static String originalFilePath = "";
    static final String RADIO = "radio";
    static final String UNDERLINE = "_";
    public static void main(String[] args){
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        try {
            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            webClient.setCssErrorHandler(new SilentCssErrorHandler());
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setRedirectEnabled(true);
            webClient.getCookieManager().setCookiesEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.waitForBackgroundJavaScript(600*1000);

            final HtmlPage rawPage = webClient.getPage("https://longyan.xueanquan.com/");
            System.out.println(webClient.getCookieManager().getCookies());

            final HtmlAnchor submitInput = rawPage.getHtmlElementById("LoginButton");
            final HtmlTextInput account = rawPage.getHtmlElementById("UName");
            final HtmlPasswordInput passwd = rawPage.getHtmlElementById("PassWord");

            account.type("luzhencheng3395");
            passwd.type("12345678a");

            final HtmlPage loginPage = submitInput.click();

            System.out.println("Login in " + loginPage);

            HtmlPage subWorkPage = webClient.getPage("https://huodong.xueanquan.com/2018traffic/family.html");

            List<HtmlAnchor> stepTwoDivElements = subWorkPage.getByXPath("//div[@data-step='2']/a");
            for(HtmlAnchor subAnchor : stepTwoDivElements){
                System.out.println(subAnchor);
                HtmlPage testPage = subAnchor.click();

                System.out.println("Open Test Page: " + testPage);

                //Read Answer File

                System.out.println("start Getting file");
                File file = FileChooserDriver.getFile();
                System.out.println("got File");

                char[] answerArray = null;
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                    for(String line; (line = bufferedReader.readLine()) != null; ){
                        answerArray = line.toCharArray();
                        System.out.println("Answer line: " + line);
                    }
                }

                //id = radio_选项(0-n)_问题no(1-n)
                if(answerArray != null){
                    for(int i = 0; i < answerArray.length; i++){
                        int tempQuestionNo = answerArray[i] - 65;
                        String questionId = new StringBuilder(RADIO).append(UNDERLINE).append(tempQuestionNo).append(UNDERLINE).append(i + 1).toString();
                        try{
                            testPage =  testPage.getHtmlElementById(questionId).click();
                            System.out.println("click on " + questionId);
                        } catch (Exception e){
                            System.out.println("Cannot find the element with the id: " + questionId);
                        }
                    }

                    List<HtmlButtonInput> list = testPage.getByXPath("//input[@value='提交']");
                    for(HtmlButtonInput input : list){
                        HtmlPage subPage = input.click();
                        System.out.println("click on submit");
                        System.out.println(subPage.getBody().asText());
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
