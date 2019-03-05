package me.flickersoul.autosafetree;

public class StartDriver {
    public static void main(String[] args){
        System.setProperty("logfile.root", System.getProperty("user.home") + "/AUTO_SAFE_TREE_LOG/");
        MainEntrance.main(args);
    }
}
