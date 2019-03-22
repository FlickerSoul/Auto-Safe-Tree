package me.flickersoul.autosafetree;

public class StartDriver {
    public static void main(String[] args){
        System.setProperty("app.version", "0.9.5.2-beta");
        System.setProperty("cache.root", System.getProperty("user.home") + "/AUTO_SAFE_TREE/");
        System.setProperty("logfile.root", System.getProperty("cache.root") + "/LOG/");
        System.setProperty("profile.root", System.getProperty("cache.root") + "/XML/");
        MainEntrance.main(args);
    }
}
