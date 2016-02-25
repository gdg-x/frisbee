package org.gdg.frisbee.android;

public class WearableConfiguration {

    private String title;
    private int icon;
    private boolean selected;

    public WearableConfiguration(int icon, String title) {
        this(icon, title, false);
    }

    public WearableConfiguration(int icon, String title, boolean selected) {
        this.icon = icon;
        this.title = title;
        this.selected = selected;
    }

    public int getIcon() {
        if ("Date".equals(title)) {
            return isSelected() ? R.drawable.ic_date_on : R.drawable.ic_date_off;
        } else {
            return icon;
        }
    }

    public String getTitle() {
        return title;
    }

    public boolean isSelected() {
        return selected;
    }
}
