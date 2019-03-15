package me.flickersoul.autosafetree;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.log4j.Level;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class MainWindowController {
    @FXML
    TextField male_account_text_field;
    @FXML
    TextField male_password_text_field;
    @FXML
    TextField female_account_text_field;
    @FXML
    TextField female_password_text_field;
    @FXML
    HBox item_list_container;
    @FXML
    Button add_test_info_button;
    @FXML
    Button add_survey_info_button;
    @FXML
    ComboBox<Integer> thread_num_combo_box;
    @FXML
    CheckBox renew_check_box;
    @FXML
    Button about_button;
    @FXML
    Button start_button;
    @FXML
    Button cancel_button;
    @FXML
    HBox list_view_container;
    @FXML
    HBox progress_bar_hbox;

    private static ProgressBar progressBar;

    ListView<ItemTemplate> itemListView;
    ListView<LogItem> infoListView;

    TestInfoEditorController testInfoEditorController;
    SurveyInfoEditorController surveyInfoEditorController;
    Stage stage = new Stage();

    private static HashMap<String, TestMember> testMembers = new HashMap<>();
    private static HashMap<String, SurveyMember> surveyMembers = new HashMap<>();

    private static BooleanProperty workingProperty = new SimpleBooleanProperty(false);

    public static Integer SYSTEM_PROCESSOR_CORE_COUNT = Runtime.getRuntime().availableProcessors();

    public static final StringConverter<Integer> converter = new StringConverter<>() {
        @Override
        public String toString(Integer object) {
            if(object == null)
                return null;
            else
                return object.toString();
        }

        @Override
        public Integer fromString(String string) {
            return Integer.valueOf(string);
        }
    };

    public void init(){
        TestItemTemplate.setMembers(testMembers);
        SurveyItemTemplate.setMembers(surveyMembers);

        infoListView = new InfoListView();
        infoListView.setPrefHeight(200);
        infoListView.setPrefWidth(1000);
        infoListView.getStyleClass().add("log-view");
        infoListView.getItems().addListener((ListChangeListener<? super LogItem>) observable -> {
            ObservableList<LogItem> items = infoListView.getItems();
            if (items.size() > 50) {
                items.remove(0, items.size() - 50);
            }
        });

        list_view_container.getChildren().add(infoListView);

        itemListView = new ItemListView();
        itemListView.setPrefHeight(200);
        itemListView.setPrefWidth(1000);

        item_list_container.getChildren().add(itemListView);
        TestInfoEditorController.setItemTemplates(itemListView.getItems());
        SurveyInfoEditorController.setItemTemplates(itemListView.getItems());

        GUILogger.setListView(infoListView);

        TestInfoEditorController.setTestMembers(testMembers);
        SurveyInfoEditorController.setTestMembers(surveyMembers);

        thread_num_combo_box.getItems().addAll(2, 4, 6, 8, 10);
        thread_num_combo_box.getSelectionModel().select(SYSTEM_PROCESSOR_CORE_COUNT);
        thread_num_combo_box.setConverter(converter);

        renew_check_box.setSelected(true);

        stage.initModality(Modality.APPLICATION_MODAL);

        workingProperty.addListener((observable, oldValue, newValue) -> {
            if(newValue){
                setUpProgressBar();
                start_button.setDisable(true);
                cancel_button.setDisable(false);
            } else {
                start_button.setDisable(false);
                cancel_button.setDisable(true);
                cancelProgressBar();
                ThreadBootstrapperTemplate.clearTaskCount();
            }
        });

    }

    @FXML
    public void chooseMaleAccount(){
        male_account_text_field.setText(FileChooserDriver.getFilePath("Select Male Students' Account File"));
    }

    @FXML
    public void chooseMalePassword(){
        male_password_text_field.setText(FileChooserDriver.getFilePath("Select Male students' Password File"));
    }

    @FXML
    public void chooserFemaleAccount(){
        female_account_text_field.setText(FileChooserDriver.getFilePath("Select Female Students' Account File"));
    }

    @FXML
    public void chooserFemalePassword(){
        female_password_text_field.setText(FileChooserDriver.getFilePath("Select Male Students' Password File"));
    }

    @FXML
    public void addTestInfo(){
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/TestInfoEditor.fxml"));

        try {
            stage.setScene(new Scene(loader.load()));
            testInfoEditorController = loader.getController();
            testInfoEditorController.init();

            MainEntrance.logDebug("Open Test Info Editor");

            stage.showAndWait();
        } catch (IOException e) {
            MainEntrance.logError(e.getMessage());
        }
    }

    @FXML
    public void addSurveyInfo(){
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/SurveyInfoEditor.fxml"));

        try {
            stage.setScene(new Scene(loader.load()));

            surveyInfoEditorController = loader.getController();

            surveyInfoEditorController.init();


            MainEntrance.logDebug("Open Survey Info Editor");

            stage.showAndWait();
        } catch (IOException e) {
            MainEntrance.logError(e.getMessage());
        }
    }

    @FXML
    public void openAbout(){
        try {
            Desktop.getDesktop().browse(new URI("https://blog.flicker-soul.me/auto-safe-tree-project/"));
        } catch (IOException e) {
            MainEntrance.logError(e.getMessage() + "\nCannot Open About Page;\nYou can Visit https://blog.flicker-soul.me/auto-safe-tree-project/ Manually. ");
        } catch (URISyntaxException e) {
            MainEntrance.logError(e.getMessage() + "\nCannot Open About Page;\nUnknown Error!");
        }
    }

    @FXML
    public void start(){
        if(checkIntegrity()) {
            workingProperty.set(true);
            ThreadBootstrapper.init(male_account_text_field.getText().trim(), male_password_text_field.getText().trim(), female_account_text_field.getText().trim(), female_password_text_field.getText().trim(), thread_num_combo_box.getSelectionModel().getSelectedItem(), renew_check_box.isSelected());
        }
    }

    private void setUpProgressBar(){
        progressBar = new ProgressBar(0);
        progressBar.setPrefSize(500, 18);
        progress_bar_hbox.getChildren().add(progressBar);
    }

    public static void switchWorkingStatus(boolean sign){
        workingProperty.set(sign);
    }

    @FXML
    public void cancel(){
        ThreadBootstrapper.terminateAllThread();
        workingProperty.set(false);
    }

    private void cancelProgressBar(){
        Platform.runLater(() ->{
            progress_bar_hbox.getChildren().remove(progressBar);
            progressBar = null;
        });
    }

    public static void setProgressBarRation(double ratio){
        if(progressBar != null)
            Platform.runLater(() -> progressBar.setProgress(ratio));
        else
            MainEntrance.logError("Progress Bar Is Null; Cannot Set Value");
    }

    @FXML
    public void report(){
        MainEntrance.logInfo("Opening Log File Folder; Composing Report Email...");

        try {
            URI mailContent = new URI(("mailto:i@flicker-soul.me?subject=Bug%20Report&body=%0D%0A%0D%0A%0D%0A" + "Please Detail The Problems AND Attach Log Files (Which Have Been Opened In Your System's File Explore) Here. Thank you! %0D%0A(请详细说明问题，并且请附上Log文件，文件夹已为您打开. 谢谢!）" + "%0D%0A%0D%0A" + "Application Version: " + System.getProperty("app.version") + "%0D%0AJava Version: " + System.getProperty("java.version") + "%0D%0ASystem Os Version: "  + System.getProperty("os.name") + "%0D%0ASystem Arch: " + System.getProperty("os.arch") + "%0D%0ASystem Version: " + System.getProperty("os.version")).replaceAll(" ", "%20"));
            Desktop.getDesktop().mail(mailContent);
            MainEntrance.logDebug("Mail Content:" + mailContent);

        } catch (Exception e){
            MainEntrance.logError("Cannot Open Mail Service\n" + e.getMessage());
        }

        try {
            Desktop.getDesktop().open(new File(System.getProperty("logfile.root")));
            MainEntrance.logDebug("Opened Log File Folder");
        } catch (IOException e) {
            MainEntrance.logError("Cannot Open Log File Folder; Please Open It Manually! \n" + e.getMessage());
        }

        MainEntrance.logInfo("Report Material Prepared.");
    }

    private boolean checkIntegrity(){
        if(male_account_text_field.getText().isBlank() && female_account_text_field.getText().isBlank()){
            AlertBox.displayError("Error", "Input Error: Account Info File(s) Is(Are) Empty", "You have to input the path of account files, either by entering manually or click the button on the right to select.");
            return false;
        }

        if(itemListView.getItems().size() == 0){
            AlertBox.displayError("Error", "Input Error: Configuration Is Empty", "You have to input the configuration by clicking the buttons above.");
            return false;
        }

        int threadNum = thread_num_combo_box.getSelectionModel().getSelectedItem();

        if(threadNum <= 0 || threadNum % 2 !=0){
            AlertBox.displayWarning("Warning", "Input Error: Thread Number Is Not Even Or Above Zero", "Thread number must be above zero and even.");
            return false;
        }

        return true;
    }

    public static HashMap<String, TestMember> getTestMembers(){
        return testMembers;
    }

    public static HashMap<String, SurveyMember> getSurveyMembers(){
        return surveyMembers;
    }

    public static TestMember getItemFromTestConfigs(String key){
        return testMembers.get(key);
    }

    public static SurveyMember getItemFromSurveyConfigs(String key){
        return surveyMembers.get(key);
    }

    public static void addAlternativeConfig(String newV, SurveyMember surveyMember){
        surveyMembers.put(newV, surveyMember);
    }

    public static BooleanProperty getWorkingProperty(){
        return workingProperty;
    }
}

class InfoListView extends ListView<LogItem> {
    private final static PseudoClass fatal = PseudoClass.getPseudoClass("fatal");
    private final static PseudoClass info  = PseudoClass.getPseudoClass("info");
    private final static PseudoClass warn  = PseudoClass.getPseudoClass("warn");
    private final static PseudoClass error = PseudoClass.getPseudoClass("error");

    public InfoListView(){
        this.getStylesheets().add(MainEntrance.class.getResource("/css/main.css").toExternalForm());

        setCellFactory(param -> new ListCell<>(){

            @Override
            protected void updateItem(LogItem item, boolean empty){
                super.updateItem(item, empty);

                pseudoClassStateChanged(info, false);
                pseudoClassStateChanged(warn, false);
                pseudoClassStateChanged(error, false);
                pseudoClassStateChanged(fatal, false);

                if (item == null || empty) {
                    setText(null);
                    return;
                }
                setText(item.getMessage());

                switch (item.getLevel()) {
                    case Level.INFO_INT: {
                        pseudoClassStateChanged(info, true);
                        break;
                    }
                    case Level.WARN_INT: {
                        pseudoClassStateChanged(warn, true);
                        break;
                    }
                    case Level.ERROR_INT: {
                        pseudoClassStateChanged(error, true);
                        break;
                    }
                    case Level.FATAL_INT: {
                        pseudoClassStateChanged(fatal, true);
                        break;
                    }
                }
            }
        });
    }
}

class ItemListView extends ListView<ItemTemplate> {
    public ItemListView(){
        this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.DELETE){
                ItemTemplate itemTemplate = getSelectionModel().getSelectedItem();

                if(itemTemplate instanceof TestItemTemplate){
                    MainWindowController.getTestMembers().remove(itemTemplate.getKey());
                } else if(itemTemplate instanceof SurveyItemTemplate) {
                    MainWindowController.getSurveyMembers().remove(itemTemplate.getKey());
                }

                this.getItems().remove(itemTemplate);
            }
        });


        setCellFactory(param -> new ListCell<>(){
            @Override
            protected void updateItem(ItemTemplate item, boolean empty){
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setOnMouseClicked(null);
                } else {
                    setText(item.getDisplay());
                    setOnMouseClicked(event -> {
                        if(event.getClickCount() == 2){
                            Stage stage = new Stage();
                            stage.initModality(Modality.APPLICATION_MODAL);

                            if(item instanceof TestItemTemplate){
                                TestItemTemplate temp = (TestItemTemplate) item;
                                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/TestInfoEditor.fxml"));
                                TestInfoEditorController testInfoEditorController;
                                try {
                                    stage.setScene(new Scene(loader.load()));
                                    testInfoEditorController = loader.getController();
                                    testInfoEditorController.editInit(temp.getItem());

                                    MainEntrance.logDebug("Open Test Info Editor And Edit On " + temp);

                                    stage.showAndWait();
                                } catch (IOException e) {
                                    MainEntrance.logError(e.getMessage());
                                }
                            } else if(item instanceof SurveyItemTemplate) {
                                SurveyItemTemplate temp = (SurveyItemTemplate) item;
                                FXMLLoader loader = new FXMLLoader(this.getClass().getResource("/fxml/SurveyInfoEditor.fxml"));
                                SurveyInfoEditorController surveyInfoEditorController;
                                try {
                                    stage.setScene(new Scene(loader.load()));
                                    surveyInfoEditorController = loader.getController();
                                    surveyInfoEditorController.editInit(temp.getItem());

                                    MainEntrance.logDebug("Open Survey Info Editor And Edit On " + temp);

                                    stage.showAndWait();
                                } catch (IOException e) {
                                    MainEntrance.logError(e.getMessage());
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}