package me.flickersoul.autosafetree;

public class ItemTemplate<S> {

    private String key;
    private String display;

    public ItemTemplate (String key, String display) {
        this.key = key;
        this.display = display;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
}
