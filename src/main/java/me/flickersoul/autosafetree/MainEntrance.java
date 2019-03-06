package me.flickersoul.autosafetree;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

public class MainEntrance extends Application {
    private static final Logger logger = Logger.getLogger(MainEntrance.class);
    private MainWindowController mainWindowController;

    public static void main(String[] args){
        launch(args);
        logger.info("==================Application Closed==================\n");
        ThreadBootstrapper.shutdownBootstrapperThreadsPools();
        ThreadBootstrapper.terminateAllThread();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        logger.info("==================Application Launched==================");
        FXMLLoader loader = new FXMLLoader(MainEntrance.class.getResource("/fxml/MainWindow.fxml"));

        Scene scene = new Scene(loader.load());

        primaryStage.setScene(scene);

        mainWindowController = loader.getController();
        mainWindowController.init();

        primaryStage.setTitle("Auto Safe Tree");

        primaryStage.show();
    }

    public static void logDebug(String message){
        logger.debug(message);
    }

    public static void logInfo(String message){
        logger.info(message);
    }

    public static void logWarning(String message){
        logger.warn(message);
    }

    public static void logError(String message){
        logger.error(message);
    }

    public static void logFatal(String message){
        logger.fatal(message);
    }

    public static Logger getLogger(){
        return logger;
    }
}
