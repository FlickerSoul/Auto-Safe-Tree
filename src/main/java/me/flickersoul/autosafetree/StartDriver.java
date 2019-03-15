package me.flickersoul.autosafetree;

public class StartDriver {
    public static void main(String[] args){
        System.setProperty("app.version", "0.9.5.0-beta");
        System.setProperty("cachefile.root", System.getProperty("user.home") + "/AUTO_SAFE_TREE/");
        System.setProperty("logfile.root", System.getProperty("cachefile.root") + "/LOG/");
        MainEntrance.main(args);
    }
}
