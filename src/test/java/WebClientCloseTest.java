import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class WebClientCloseTest {
    public static void main(String[] args) throws IOException {
        System.out.println("start");

        WebClient webClient = new WebClient(BrowserVersion.CHROME);
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
        webClient.setHTMLParserListener(null);

        HtmlPage page = webClient.getPage("https://baidu.com");

        System.out.println(page.getWebResponse().getContentAsString());

        HtmlPage page1 = webClient.getPage("https://mail.163.com");

        System.out.println(page1.getWebResponse().getContentAsString());

        webClient.close();

        System.out.println(page.getWebResponse().getContentAsString());
        System.out.println(page1.getWebResponse().getContentAsString());


        webClient = new WebClient(BrowserVersion.CHROME);
        HtmlPage page3 = webClient.getPage("https://baidu.com");

        System.out.println(page3.getWebResponse().getContentAsString());

        HtmlPage page4 = webClient.getPage("https://mail.163.com");

        System.out.println(page4.getWebResponse().getContentAsString());
    }
}
