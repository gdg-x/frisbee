package org.gdg.frisbee.android.api.model;

public class Image {
    private String url;
    private boolean isDefault;

    public String getUrl() {
        return url;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
