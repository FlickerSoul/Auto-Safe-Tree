import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

public class TRTest {
    public static void main(String[] args){
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        try {
//            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.setCssErrorHandler(new SilentCssErrorHandler());
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
//            webClient.getOptions().setJavaScriptEnabled(true);
//            webClient.getOptions().setCssEnabled(false);
//            webClient.getOptions().setRedirectEnabled(true);
//            webClient.getCookieManager().setCookiesEnabled(true);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.waitForBackgroundJavaScript(600 * 1000);

            System.out.println("init finished");

            HtmlPage page = webClient.getPage(TRTest.class.getResource("/testFiles/tr.html"));

            System.out.println("loaded html page");

            System.out.println(page.getWebResponse().getContentAsString() + "\n===========================\n\n\n");

            List<HtmlElement> elements = page.getByXPath("//tr[@class='tr_wwc']/td[3][contains(text(), '专题活动')]/../td[7]/a");//*[@id="mun_-1_1"]/tr[1]/td[7]/a

            System.out.println(elements.get(0).getTextContent().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
