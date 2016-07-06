package org.gdg.frisbee.android.api.model.plus;

import java.util.List;

public class Attachment {
    private String objectType;
    private String displayName;
    private String url;
    private Image image;
    private Image fullImage;
    private List<Thumbnail> thumbnails;
    private String content;

    public String getObjectType() {
        return objectType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public Image getImage() {
        return image;
    }

    public Image getFullImage() {
        return fullImage;
    }

    public List<Thumbnail> getThumbnails() {
        return thumbnails;
    }

    public String getContent() {
        return content;
    }
}
