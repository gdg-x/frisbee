package org.gdg.frisbee.android.api.model;

import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 19:47
 * To change this template use File | Settings | File Templates.
 */
public class GdlShow {
    private String mUrl, mTitle, mYoutubeId;
    private DateTime mDateTime;

    public GdlShow() {
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public DateTime getDateTime() {
        return mDateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.mDateTime = dateTime;
    }

    public String getYoutubeId() {
        return mYoutubeId;
    }

    public void setYoutubeId(String youtubeId) {
        this.mYoutubeId = youtubeId;
    }

    public String getMaxResThumbnail() {
        return String.format("http://img.youtube.com/vi/%s/maxresdefault.jpg", getYoutubeId());
    }

    public String getMediumQualityThumbnail() {
        return String.format("http://img.youtube.com/vi/%s/mqdefault.jpg", getYoutubeId());
    }

    public String getHighQualityThumbnail() {
        return String.format("http://img.youtube.com/vi/%s/hqdefault.jpg", getYoutubeId());
    }

    public String getYoutubeUrl() {
        return String.format("http://www.youtube.com/watch?v=%s", getYoutubeId());
    }
}
