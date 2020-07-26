package com.example.camscan.Objects;

public class NavMenuObject {
    private String Id;
    private boolean isExpandable;
    private String title;
    private int iconId;
    private boolean isVisible;

    public NavMenuObject(boolean isExpandable, String title,int iconId,boolean isVisible) {
        this.isExpandable = isExpandable;
        this.title = title;
        this.iconId=iconId;
        this.isVisible=isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public boolean isExpandable() {
        return isExpandable;
    }

    public void setExpandable(boolean expandable) {
        isExpandable = expandable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
