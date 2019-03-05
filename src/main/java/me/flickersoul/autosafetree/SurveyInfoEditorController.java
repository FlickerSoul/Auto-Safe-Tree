package me.flickersoul.autosafetree;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SurveyInfoEditorController {
    private static HashMap<String, SurveyMember> surveyMembers;

    private final static Pattern RADIO_PATTERN = Pattern.compile("//(\\w+)\\[@(\\w+)='(\\w+?)(No.|ABC)(\\w+?)(No.|ABC)'\\]");

    public static void setTestMembers(HashMap<String, SurveyMember> list){
        surveyMembers = list;
    }

    private static ObservableList<ItemTemplate> itemTemplates;

    public static void setItemTemplates(ObservableList<ItemTemplate> templates){
        itemTemplates = templates;
    }

    @FXML
    TextField page_url_text_field;
    @FXML
    TextField male_student_answers_path_text_field;
    @FXML
    TextField female_student_answers_path_text_field;
    @FXML
    Button male_answer_button;
    @FXML
    Button female_answer_button;
    @FXML
    TextField first_button_xpath_text_field;
    @FXML
    TextField second_button_xpath_text_field;
    @FXML
    ComboBox<String> radio_combo_box;
    @FXML
    ComboBox<String> no_or_abc_1_combo_box;
    @FXML
    ComboBox<String> link_combo_box;
    @FXML
    ComboBox<String> no_or_abc_2_combo_box;
    @FXML
    ComboBox<String> tag_combo_box;
    @FXML
    ComboBox<String> param_combo_box;
    @FXML
    ComboBox<Integer> no_starts_combo_box;
    @FXML
    ComboBox<Integer> abc_starts_combo_box;
    @FXML
    TextField next_page_xpath_text_field;
    @FXML
    TextField click_interval_text_field;
    @FXML
    TextField submit_button_xpath_text_field;
    @FXML
    Button submit_button;

    public SurveyInfoEditorController(){
        MainEntrance.logDebug("Open Survey Editor");
    }

    public void init(){
        MainEntrance.logDebug("Start init Editor");
        radio_combo_box.getSelectionModel().select("radio_");
        no_or_abc_1_combo_box.getItems().addAll("No.", "ABC");
        link_combo_box.getSelectionModel().select("_");
        no_or_abc_2_combo_box.getItems().addAll("No.", "ABC");
        tag_combo_box.getItems().addAll("input", "a");
        param_combo_box.getItems().addAll("id", "name");
        no_starts_combo_box.getItems().addAll(0, 1);
        abc_starts_combo_box.getItems().addAll(0, 1);

        no_starts_combo_box.setConverter(MainWindowController.converter);
        abc_starts_combo_box.setConverter(MainWindowController.converter);

        MainEntrance.logDebug("init Selector");

        submit_button.setOnMouseClicked(event -> submit());
        MainEntrance.logDebug("init buttons");

    }

    public void editInit(SurveyMember member){
        init();

        String pageUrl = member.getPageUrl();
        String maleAnswerPath = member.getMaleAnswerPath();
        String femaleAnswerPath = member.getFemaleAnswerPath();
        String firstButtonXPath = member.getFirstButtonXPath();
        String secondButtonXPath = member.getSecondButtonXPath();
        String selectorXPath = member.getSelectorXPath();
        String nextPageXPath = member.getNextPageXPath();
        int clickInterval = member.getClickInterval();
        String submitButtonXPath = member.getSubmitButtonXPath();
        MainEntrance.logDebug("Got Info From Member: " + member);

        page_url_text_field.setText(pageUrl);
        male_student_answers_path_text_field.setText(maleAnswerPath);
        female_student_answers_path_text_field.setText(femaleAnswerPath);
        first_button_xpath_text_field.setText(firstButtonXPath);
        second_button_xpath_text_field.setText(secondButtonXPath);
        Matcher result = RADIO_PATTERN.matcher(selectorXPath);
        if(result.find()){
            tag_combo_box.getSelectionModel().select(result.group(1));
            param_combo_box.getSelectionModel().select(result.group(2));
            radio_combo_box.getSelectionModel().select(result.group(3));
            no_or_abc_1_combo_box.getSelectionModel().select(result.group(4));
            link_combo_box.getSelectionModel().select(result.group(5));
            no_or_abc_2_combo_box.getSelectionModel().select(result.group(6));
            no_starts_combo_box.getSelectionModel().select(Integer.valueOf(member.getNoStartPoint()));
            abc_starts_combo_box.getSelectionModel().select(Integer.valueOf(member.getAbcStartPoint()));
        }
        next_page_xpath_text_field.setText(nextPageXPath);
        click_interval_text_field.setText(String.valueOf(clickInterval));
        submit_button_xpath_text_field.setText(submitButtonXPath);
        MainEntrance.logDebug("Put Every Param Into The Right Places");

        submit_button.setOnMouseClicked(event -> {
            if(checkIntegrity()) {
                member.updateInfo(page_url_text_field.getText(), male_student_answers_path_text_field.getText(), female_student_answers_path_text_field.getText(), first_button_xpath_text_field.getText(), second_button_xpath_text_field.getText(), getSelectorXPath(), next_page_xpath_text_field.getText(), click_interval_text_field.getText(), submit_button_xpath_text_field.getText(), Integer.valueOf(no_starts_combo_box.getSelectionModel().getSelectedItem()), Integer.valueOf(abc_starts_combo_box.getSelectionModel().getSelectedItem()));
                MainEntrance.logInfo("Update Info Successfully!");
            }
        });
        MainEntrance.logDebug("Rewrite Submit Button Action");
    }

    public void submit(){
        if(checkIntegrity()) {
            MainEntrance.logDebug("Every item Is Completed");
            final String url = page_url_text_field.getText();
            MainEntrance.logDebug("This Configuration Is Under URL: " + url);

            if(surveyMembers.containsKey(url)){
                AlertBox.displayError("Error", "Input Error: URL Has Been In Configurations", "You have to input a different URL to identify this configuration.");
                return;
            }
            MainEntrance.logDebug("This Configuration Is New; Adding To Configuration List");

            int noStartPoint = no_starts_combo_box.getSelectionModel().getSelectedItem();
            int abcStartPoint = abc_starts_combo_box.getSelectionModel().getSelectedItem();
            MainEntrance.logDebug("The Test Serial Starts At " + noStartPoint + "; The Answer Starts At " + abcStartPoint);

            SurveyMember surveyMember = new SurveyMember(page_url_text_field.getText(), male_student_answers_path_text_field.getText(), female_student_answers_path_text_field.getText(), first_button_xpath_text_field.getText(), second_button_xpath_text_field.getText(), getSelectorXPath(), next_page_xpath_text_field.getText(), click_interval_text_field.getText(), submit_button_xpath_text_field.getText(), noStartPoint, abcStartPoint);
            surveyMembers.put(url, surveyMember);
            itemTemplates.add(new SurveyItemTemplate(url, page_url_text_field.getText()));

            MainEntrance.logInfo("Add New Survey Member -- " + surveyMember);

            submit_button.getScene().getWindow().hide();
            MainEntrance.logDebug("Close Window");
        }
    }

    private String getSelectorXPath() {
        String selectorXPath = "//" + tag_combo_box.getSelectionModel().getSelectedItem() + "[@" + param_combo_box.getSelectionModel().getSelectedItem() + "='" + radio_combo_box.getSelectionModel().getSelectedItem() + no_or_abc_1_combo_box.getSelectionModel().getSelectedItem() + link_combo_box.getSelectionModel().getSelectedItem() + no_or_abc_2_combo_box.getSelectionModel().getSelectedItem() + "']";;
        MainEntrance.logInfo("Selector XPath is " + selectorXPath);
        return selectorXPath;
    }

    private boolean checkIntegrity() {
        if(page_url_text_field.getText().equals("")){
            AlertBox.displayError("Error", "Input Error: Page URL Is Empty", "You have to input page url manually");
            return false;
        }

        if(male_student_answers_path_text_field.getText().equals("") || female_student_answers_path_text_field.getText().equals("")){
            AlertBox.displayError("Error", "Input Error: Answer File(s) Is(Are) Missing", "You have to input all the file required");
            return false;
        }

        if(no_starts_combo_box.getSelectionModel().getSelectedItem() == null || abc_starts_combo_box.getSelectionModel().getSelectedItem() == null){
            AlertBox.displayError("Error", "Input Error: Starts Point(s)", "You have to select or input a value for the start points");
            return false;
        }

        String radioText = radio_combo_box.getSelectionModel().getSelectedItem();
        String selection1 = no_or_abc_1_combo_box.getSelectionModel().getSelectedItem();
        String selection2 = no_or_abc_2_combo_box.getSelectionModel().getSelectedItem();
        String linkText = link_combo_box.getSelectionModel().getSelectedItem();

        if(radioText == null || radioText.equals("")){
            AlertBox.displayError("Error", "Input Error: No Value In Radio ComboBox", "You have to input a value to the selector");
            return false;
        }

        if(linkText == null || linkText.equals("")){
            AlertBox.displayError("Error", "Input Error: No Value In Link ComboBox", "You have to input a value to the selector");
            return false;
        }

        if(selection1 == null || selection1.equals(selection2)){
            AlertBox.displayError("Error", "Input Error: Same Param In Alternative Value Selector", "You inputted the same parameter in value selector!");
            MainEntrance.logError("");
            return false;
        }

        if(first_button_xpath_text_field.getText().equals("") || second_button_xpath_text_field.getText().equals("") || submit_button_xpath_text_field.getText().equals("")){
            AlertBox.displayError("Error", "Input Error: XPath(s) Is(Are) Empty", "You have to complete all the XPaths of buttons");
            return false;
        }

        return true;
    }

    public void getMaleAnswers(){
        male_student_answers_path_text_field.setText(FileChooserDriver.getFilePath("Select Male Students' Answer File"));
    }

    public void getFemaleAnswers(){
        female_student_answers_path_text_field.setText(FileChooserDriver.getFilePath("Select Female Students' Answer File"));
    }

}
