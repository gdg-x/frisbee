package org.gdg.frisbee.android.api.model;


public class EventDetail {
    private String mId, mTitle, mGplusEventUrl, mAbout;

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getGplusEventUrl() {
        return mGplusEventUrl;
    }

    public void setGplusEventUrl(String mGplusEvent) {
        this.mGplusEventUrl = mGplusEvent;
    }

    public String getAbout() {
        return mAbout;
    }

    public void setAbout(String mAbout) {
        this.mAbout = mAbout;
    }
}
