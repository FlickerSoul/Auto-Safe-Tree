package me.flickersoul.autosafetree;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestInfoEditorController {
    private static HashMap<String, TestMember> testMembers;

    private final static Pattern VALUE_PATTERN = Pattern.compile("@value='(\\d+)'");
    private final static Pattern RADIO_PATTERN = Pattern.compile("//(\\w+)\\[@(\\w+)='(\\w+?)(No.|ABC)(\\w+?)(No.|ABC)'\\]");

    public static void setTestMembers(HashMap<String, TestMember> list){
        testMembers = list;
    }

    private static ObservableList<ItemTemplate> itemTemplates;

    public static void setItemTemplates(ObservableList<ItemTemplate> templates){
        itemTemplates = templates;
    }

    @FXML
    TextField page_url_text_field;
    @FXML
    TextField first_button_XPath_text_field;
    @FXML
    TextField second_button_XPath_text_field;
    @FXML
    HBox value_selector_hbox;
    @FXML
    CheckBox value_selector_check_box;
    @FXML
    TextField value_selector_text_field;
    @FXML
    HBox alternative_selector_hbox;
    @FXML
    ComboBox<String> radio_combo_box;
    @FXML
    ComboBox<String> no_or_abc_1_combo_box;
    @FXML
    ComboBox<String> link_combo_box;
    @FXML
    ComboBox<String> no_or_abc_2_combo_box;
    @FXML
    ComboBox<Integer> no_starts_combo_box;
    @FXML
    ComboBox<Integer> abc_starts_combo_box;
    @FXML
    CheckBox alternative_selector_check_box;
    @FXML
    HBox starts_hbox;
    @FXML
    TextField open_new_page_XPath_text_field;
    @FXML
    TextField click_interval_text_field;
    @FXML
    TextField submit_button_XPath_text_field;
    @FXML
    ComboBox<String> alternative_tag_combo_box;
    @FXML
    ComboBox<String> tag_combo_box;
    @FXML
    ComboBox<String> param_combo_box;
    @FXML
    Button submit_button;


    public void init(){
        value_selector_check_box.selectedProperty().addListener((observable, oldValue, newValue) -> {
            value_selector_hbox.setDisable(oldValue);
        });

        alternative_selector_check_box.selectedProperty().addListener((observable, oldValue, newValue) -> {
            alternative_selector_hbox.setDisable(oldValue);
            starts_hbox.setDisable(true);
        });

        value_selector_check_box.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && alternative_selector_check_box.isSelected() && !tag_combo_box.getSelectionModel().getSelectedItem().equals(alternative_tag_combo_box.getSelectionModel().getSelectedItem()))
                AlertBox.displayWarning("Warning", "The Tags Declared In Box Section Should Be The Same", "Since you have toggle the alternative selector for assisting the selecting process, it's necessary that the tag names in both selectors are the same.");

        });

        alternative_selector_check_box.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue && value_selector_check_box.isSelected() && !tag_combo_box.getSelectionModel().getSelectedItem().equals(alternative_tag_combo_box.getSelectionModel().getSelectedItem()))
                AlertBox.displayWarning("Warning", "The Tags Declared In Box Section Should Be The Same", "Since you have toggle the alternative selector for assisting the selecting process, it's necessary that the tag names in both selectors are the same.");

        });

        MainEntrance.logDebug("init HBOXs bindings");

        tag_combo_box.getSelectionModel().select("input");
        MainEntrance.logDebug("init Selector");

        radio_combo_box.getSelectionModel().select("radio_");
        no_or_abc_1_combo_box.getItems().addAll("No.", "ABC");
        link_combo_box.getSelectionModel().select("_");
        no_or_abc_2_combo_box.getItems().addAll("No.", "ABC");
        alternative_tag_combo_box.getSelectionModel().select("input");
        param_combo_box.getSelectionModel().select("id");
        no_starts_combo_box.getItems().addAll(0, 1);
        abc_starts_combo_box.getItems().addAll(0, 1);

        alternative_selector_check_box.setDisable(true); //暂时禁用

        no_starts_combo_box.setConverter(MainWindowController.converter);
        abc_starts_combo_box.setConverter(MainWindowController.converter);
        MainEntrance.logDebug("init Alternative Selector");

        submit_button.setOnMouseClicked(event -> submit());
        MainEntrance.logDebug("init submit button");
    }

    public void editInit(TestMember member){
        String firstButtonXPath = member.getFirstButtonXPath();
        String secondButtonXPath = member.getSecondButtonXPath();
        String selectorXPath = member.getSelectorXPath();
        String nextPageXPath = member.getNextPageXPath();
        int clickInterval = member.getClickInterval();
        String submitButtonXPath = member.getSubmitButtonXPath();
        init();

        first_button_XPath_text_field.setText(firstButtonXPath);
        second_button_XPath_text_field.setText(secondButtonXPath);
        //用正则表达获取信息。。。
        if(selectorXPath.contains("value")){
            value_selector_check_box.setSelected(true);
            Matcher result = VALUE_PATTERN.matcher(selectorXPath);

            if(result.find()){
                value_selector_text_field.setText(result.group(1));
            }

        } else {
            value_selector_check_box.setSelected(false);
            Matcher result = RADIO_PATTERN.matcher(selectorXPath);

            //修改选择逻辑

            if(result.find()){
                alternative_tag_combo_box.getSelectionModel().select(result.group(1));
                param_combo_box.getSelectionModel().select(result.group(2));
                radio_combo_box.getSelectionModel().select(result.group(3));
                no_or_abc_1_combo_box.getSelectionModel().select(result.group(4));
                link_combo_box.getSelectionModel().select(result.group(5));
                no_or_abc_2_combo_box.getSelectionModel().select(result.group(6));
            }

        }
        open_new_page_XPath_text_field.setText(nextPageXPath);
        click_interval_text_field.setText(String.valueOf(clickInterval));
        submit_button_XPath_text_field.setText(submitButtonXPath);

        MainEntrance.logDebug("init Info Filling");

        submit_button.setOnMouseClicked(event -> {
            if(checkIntegrity()) {
                member.updateInfo(page_url_text_field.getText().trim(), first_button_XPath_text_field.getText().trim(), second_button_XPath_text_field.getText().trim(), getSelectorXPath(), open_new_page_XPath_text_field.getText().trim(), click_interval_text_field.getText().trim(), submit_button_XPath_text_field.getText().trim());
                MainEntrance.logInfo("Update Info Successfully!");
            }
        });

        MainEntrance.logDebug("finish Button Method Transform");
    }

    @FXML
    public void submit(){
        if(checkIntegrity()) {
            String url = page_url_text_field.getText().trim();

            if(url.equals("") && testMembers.containsKey(url)){
                AlertBox.displayError("Error", "URL Input Error: Global Config For Tests Has Been Set", "You've inputted a global config (which has empty url set); you have to input a url to identify this config.");
                return;
            }

            TestMember testMember = new TestMember(url, first_button_XPath_text_field.getText().trim(), second_button_XPath_text_field.getText().trim(), getSelectorXPath(), open_new_page_XPath_text_field.getText().trim(), click_interval_text_field.getText().trim(), submit_button_XPath_text_field.getText().trim());
            testMembers.put(url, testMember);
            itemTemplates.add(new TestItemTemplate(url, "Test Info"));

            MainEntrance.logInfo("Add New Test Member -- " + testMember);

            submit_button_XPath_text_field.getScene().getWindow().hide();
        }
    }

    private String getSelectorXPath(){
        String selectorXPath = null;
        if(value_selector_check_box.isSelected()){
            String number = value_selector_text_field.getText();
            selectorXPath = "//" + tag_combo_box.getSelectionModel().getSelectedItem() + "[@value='" + number +"']";
        } else if(alternative_selector_check_box.isSelected()) {
            selectorXPath = "//" + alternative_tag_combo_box.getSelectionModel().getSelectedItem() + "[@" + param_combo_box.getSelectionModel().getSelectedItem() + "='" + radio_combo_box.getSelectionModel().getSelectedItem() + no_or_abc_1_combo_box.getSelectionModel().getSelectedItem() + link_combo_box.getSelectionModel().getSelectedItem() + no_or_abc_2_combo_box.getSelectionModel().getSelectedItem() + "']";
        }

        MainEntrance.logInfo("Selector XPath is " + selectorXPath);
        return selectorXPath;
    }

    private boolean checkIntegrity(){
        if(page_url_text_field.getText().isBlank())
            MainEntrance.logWarning("The Page URL For This Test Is Empty! This Configuration Is Used For All Pages That Don't Have URL Specified!");

        String secondButtonXPath = second_button_XPath_text_field.getText();

        if(secondButtonXPath.isBlank()){
            AlertBox.displayError("Error", "Input Error: Empty Second Button XPath", "You have to input a XPath for entering test!");
            return false;
        }

        if(value_selector_check_box.isSelected()){
            if(value_selector_text_field.getText().isBlank() || tag_combo_box.getSelectionModel().getSelectedItem().isBlank()){
                AlertBox.displayError("Error", "Input Error: No Value In The First Selector", "You have to input a value to the selector");
                return false;
            }
        }

        if(alternative_selector_check_box.isSelected()) {
            String selection1 = no_or_abc_1_combo_box.getSelectionModel().getSelectedItem();
            String selection2 = no_or_abc_2_combo_box.getSelectionModel().getSelectedItem();

            if (selection1 == null || selection1.equals(selection2)) {
                AlertBox.displayError("Error", "Input Error: Same Param In Alternative Value Selector", "You inputted the same parameter in value selector!");
                return false;
            }

            String radioText = radio_combo_box.getSelectionModel().getSelectedItem();
            String linkText = link_combo_box.getSelectionModel().getSelectedItem();

            if(radioText == null || radioText.isBlank()){
                AlertBox.displayError("Error", "Input Error: No Value In Radio ComboBox", "You have to input a value to the selector");
                return false;
            }

            if(linkText == null || linkText.isBlank()){
                AlertBox.displayError("Error", "Input Error: No Value In Link ComboBox", "You have to input a value to the selector");
                return false;
            }

            String tagName = alternative_tag_combo_box.getSelectionModel().getSelectedItem();
            String param = param_combo_box.getSelectionModel().getSelectedItem();

            if(tagName.isBlank() || param.isBlank()){
                if(linkText.isBlank()){
                    AlertBox.displayError("Error", "Input Error: No Value In Tag Or Parameter ComboBox", "You have to input a value to the selector");
                    return false;
                }
            }

            if(value_selector_check_box.isSelected() && !tag_combo_box.getSelectionModel().getSelectedItem().equals(tagName)){
                AlertBox.displayError("Error", "Input Error: Different Tag In Selectors", "Since you have toggle the alternative selector for assisting the selecting process, it's necessary that the tag names in both selectors are the same.");
                return false;
            }
        }

        String submitButtonXPath = submit_button_XPath_text_field.getText();

        if(submitButtonXPath.isBlank()){
            AlertBox.displayError("Error", "Input Error: Empty Submit Button XPath", "You have to input a XPath for submitting test!");
            return false;
        }

        return true;
    }
}
