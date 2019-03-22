package me.flickersoul.autosafetree;

import java.io.*;

public class SurveyMember  {
    private String pageUrl;
    private String maleAnswerPath;
    private String femaleAnswerPath;
    private String firstButtonXPath;
    private String secondButtonXPath;
    private String selectorXPath;
    private String nextPageXPath;
    private int clickInterval;
    private String submitButtonXPath;
    private String femaleAnswerLine;
    private String maleAnswerLine;
    private int noStartPoint;
    private int abcStartPoint;


    public SurveyMember(String pageUrl, String maleAnswerPath, String femaleAnswerPath, String firstButtonXPath, String secondButtonXPath, String selectorXPath, String nextPageXPath, String clickInterval, String submitButtonXPath, int noStartPoint, int abcStartPoint) {
        this.pageUrl = pageUrl;
        this.maleAnswerPath = maleAnswerPath;
        this.femaleAnswerPath = femaleAnswerPath;
        this.firstButtonXPath = firstButtonXPath;
        this.secondButtonXPath = secondButtonXPath;
        this.selectorXPath = selectorXPath;
        this.nextPageXPath = nextPageXPath;
        this.clickInterval = Integer.valueOf(clickInterval.equals("") ? "0" : clickInterval);
        this.submitButtonXPath = submitButtonXPath;
        this.noStartPoint = noStartPoint;
        this.abcStartPoint = abcStartPoint;

        MainEntrance.logDebug(super.toString() + ": URL: "  + pageUrl + "; Male Answers: " + maleAnswerLine + "; Female Answers: " + femaleAnswerLine + "; First Button XPath: " + firstButtonXPath + "; Second Button XPath: " + secondButtonXPath);
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

    public String getMaleAnswerPath() {
        return maleAnswerPath;
    }

    public String getFemaleAnswerPath() {
        return femaleAnswerPath;
    }

    @Override
    public String toString(){
        return "Survey On Page: " + pageUrl;
    }

    public String getFemaleAnswerLine() {
        return femaleAnswerLine;
    }

    public String getMaleAnswerLine() {
        return maleAnswerLine;
    }

    public int getNoStartPoint() {
        return noStartPoint;
    }

    public int getAbcStartPoint() {
        return abcStartPoint;
    }

    public boolean setAnswers(){
        maleAnswerLine = getAnswerLine(maleAnswerPath);
        femaleAnswerLine = getAnswerLine(femaleAnswerPath);

        if(femaleAnswerLine.isBlank() && maleAnswerLine.isBlank()){
            return false;
        } else {
            if(femaleAnswerLine.isBlank())
                femaleAnswerLine = maleAnswerLine;
            if(maleAnswerLine.isBlank())
                maleAnswerLine = femaleAnswerLine;

            MainEntrance.logDebug("Male Answer: " + maleAnswerLine + "; Female Answer Is: " + femaleAnswerLine);
            return true;
        }
    }

    private String getAnswerLine(String path){
        File answerFile = new File(path);

        if(answerFile.canRead()) {
            try (BufferedReader maleAnswerBufferedReader = new BufferedReader(new FileReader(answerFile))) {
                String answer = maleAnswerBufferedReader.readLine();
                if (maleAnswerBufferedReader.readLine() != null)
                    throw new Exception("Malformat File");
                if (answer != null && !answer.isBlank())
                    return answer;

            } catch (FileNotFoundException e) {
                AlertBox.displayError("Error", "File Error: Answer File Is Not Found", "Please recheck the file path. \nGo Back And Edit This Configuration");
            } catch (IOException e) {
                AlertBox.displayError("Error", "IO Error: Cannot Read The Answer File", "Please ensure you have the access to the file. \nGo Back And Edit This Configuration");
            } catch (Exception e) {
                AlertBox.displayError("Error", "File Error: Answer File Should Has More Than One Line", "Please check the answer file and ensure it has only one line. \nGo Back And Edit This Configuration");
            }

            return "";
        } else {
            return  path;
        }
    }

    public void updateInfo(String pageUrl, String maleAnswerPath, String femaleAnswerPath, String firstButtonXPath, String secondButtonXPath, String selectorXPath, String nextPageXPath, String clickInterval, String submitButtonXPath, int noStartPoint, int abcStartPoint) {
        this.pageUrl = pageUrl;
        this.maleAnswerPath = maleAnswerPath;
        this.femaleAnswerPath = femaleAnswerPath;
        this.firstButtonXPath = firstButtonXPath;
        this.secondButtonXPath = secondButtonXPath;
        this.selectorXPath = selectorXPath;
        this.nextPageXPath = nextPageXPath;
        this.clickInterval = Integer.valueOf(clickInterval);
        this.submitButtonXPath = submitButtonXPath;
        this.noStartPoint = noStartPoint;
        this.abcStartPoint = abcStartPoint;

        MainEntrance.logDebug(super.toString() + ": URL: "  + pageUrl + "; Male Answers: " + maleAnswerLine + "; Female Answers: " + femaleAnswerLine + "; First Button XPath: " + firstButtonXPath + "; Second Button XPath: " + secondButtonXPath);
    }
}
