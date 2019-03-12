package me.flickersoul.autosafetree;

import java.io.*;

import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class FileChooserDriver {
    public static File getFile(Stage stage){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text File", "*.text"));
        fileChooser.setTitle("Open Text File");
        return fileChooser.showOpenDialog(stage);
    }

    public static String getFilePath(String text){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text File", "*.text"));
        fileChooser.setTitle(text);
        File file = fileChooser.showOpenDialog(stage);
        if(file == null)
            return "";

        return file.toURI().toString();
    }

    public static File getFile(){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text File", "*.text"));
        fileChooser.setTitle("Open File");
        return fileChooser.showOpenDialog(stage);
    }

    public static String getAnswerFromFile(){
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text File", "*.text"));
        fileChooser.setTitle("Get Answers From Files");

        File file = fileChooser.showOpenDialog(stage);

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String answer =  bufferedReader.readLine();
            if(bufferedReader.readLine() != null)
                throw new Exception("Malformat File");
            if(answer != null && answer != "")
                return answer;
        } catch (FileNotFoundException e) {
            AlertBox.displayError("Error", "File Error: Answer File Is Not Found", "Please recheck the file path.");
        } catch (IOException e) {
            AlertBox.displayError("Error", "IO Error: Cannot Read The File", "Please ensure you have the access to the file.");
        } catch (Exception e) {
            AlertBox.displayError("Error", "File Error: Answer File Should Has More Than One Line", "Please check the answer file and ensure it has only one line.");
        }

        return null;
    }
}
