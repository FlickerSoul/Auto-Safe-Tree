package me.flickersoul.autosafetree;

public class StartDriver {
    public static void main(String[] args){
        System.setProperty("app.version", "0.9.5.2-beta");
        System.setProperty("cachefile.root", System.getProperty("user.home") + "/AUTO_SAFE_TREE/");
        System.setProperty("logfile.root", System.getProperty("cachefile.root") + "/LOG/");
        MainEntrance.main(args);
        //JLink Command; In JDK Home Folder
        //bin/jlink --module-path jmods --add-modules java.base,javafx.base,javafx.graphics,javafx.controls,javafx.fxml,java.desktop,javafx.media,java.naming,java.xml,java.scripting,java.datatransfer,java.logging,jdk.xml.dom,java.management,java.sql,java.security.jgss --output C:\workspace\publish\jre\11
    }
}
