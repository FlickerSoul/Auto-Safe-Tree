package me.flickersoul.autosafetree;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

public class GUILogger extends WriterAppender {
    private static ListView<LogItem> listView;


    public static void setListView(ListView<LogItem> view){
        listView = view;
    }

    @Override
    public void append(LoggingEvent event) {
        Platform.runLater(() -> listView.getItems().add(new LogItem(event.getMessage().toString(), event.getLevel().toInt())));
    }
}
