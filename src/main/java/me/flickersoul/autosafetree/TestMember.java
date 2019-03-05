package me.flickersoul.autosafetree;

public class TestMember {
    private String pageUrl;
    private String firstButtonXPath;
    private String secondButtonXPath;
    private String selectorXPath;
    private String nextPageXPath;
    private int clickInterval;
    private String submitButtonXPath;
    private int noStartPoint;
    private int abcStartPoint;

    public TestMember(String pageUrl, String firstButtonXPath, String secondButtonXPath, String selectorXPath, String nextPageXPath, String clickInterval, String submitButtonXPath) {
        this.pageUrl = pageUrl;
        this.firstButtonXPath = firstButtonXPath;
        this.secondButtonXPath = secondButtonXPath;
        this.selectorXPath = selectorXPath;
        this.nextPageXPath = nextPageXPath;
        this.clickInterval = Integer.valueOf(clickInterval.equals("") ? "0" : clickInterval);
        this.submitButtonXPath = submitButtonXPath;
    }

    public TestMember(String pageUrl, String firstButtonXPath, String secondButtonXPath, String selectorXPath, String nextPageXPath, int clickInterval, String submitButtonXPath, int noStartPoint, int abcStartPoint) {
        this.pageUrl = pageUrl;
        this.firstButtonXPath = firstButtonXPath;
        this.secondButtonXPath = secondButtonXPath;
        this.selectorXPath = selectorXPath;
        this.nextPageXPath = nextPageXPath;
        this.clickInterval = clickInterval;
        this.submitButtonXPath = submitButtonXPath;
        this.noStartPoint = noStartPoint;
        this.abcStartPoint = abcStartPoint;
    }

    public String getFirstButtonXPath() {
        return firstButtonXPath;
    }

    public String getSecondButtonXPath() {
        return secondButtonXPath;
    }

    public String getSelectorXPath() {
        return selectorXPath;
    }

    public String getNextPageXPath() {
        return nextPageXPath;
    }

    public int getClickInterval() {
        return clickInterval;
    }

    public String getSubmitButtonXPath() {
        return submitButtonXPath;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public int getNoStartPoint() {
        return noStartPoint;
    }

    public int getAbcStartPoint() {
        return abcStartPoint;
    }

    @Override
    public String toString(){
        return "Test Page URL: " + pageUrl;
    }

    public void updateInfo(String pageUrl, String firstButtonXPath, String secondButtonXPath, String selectorXPath, String nextPageXPath, String clickInterval, String submitButtonXPath) {
        this.pageUrl = pageUrl;
        this.firstButtonXPath = firstButtonXPath;
        this.secondButtonXPath = secondButtonXPath;
        this.selectorXPath = selectorXPath;
        this.nextPageXPath = nextPageXPath;
        this.clickInterval = Integer.valueOf(clickInterval.equals("") ? "0" : clickInterval);
        this.submitButtonXPath = submitButtonXPath;
    }
}
