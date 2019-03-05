package me.flickersoul.autosafetree;

public class LogItem {
    private final String message;
    private final int level;

    public LogItem(String message, int level) {
        this.message = message;
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public int getLevel() {
        return level;
    }
}
