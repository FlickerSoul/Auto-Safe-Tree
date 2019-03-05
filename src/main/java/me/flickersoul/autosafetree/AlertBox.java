package me.flickersoul.autosafetree;

import javafx.scene.control.Alert;

public class AlertBox {

    protected static void displayError(String title, String headerText, String content){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        MainEntrance.logError(headerText);

        alert.showAndWait();
    }

    protected static void displayWarning(String title, String headerText, String content){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        MainEntrance.logWarning(headerText);

        alert.showAndWait();
    }
}

