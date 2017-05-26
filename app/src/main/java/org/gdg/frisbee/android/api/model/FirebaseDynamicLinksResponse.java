package org.gdg.frisbee.android.api.model;

/**
 * Created by unstablebrainiac on 26/5/17.
 */

public class FirebaseDynamicLinksResponse {
    private String shortLink;
    private String previewLink;

    public FirebaseDynamicLinksResponse(String shortLink, String previewLink) {
        this.shortLink = shortLink;
        this.previewLink = previewLink;
    }

    public String getShortLink() {
        return shortLink;
    }

    public void setShortLink(String shortLink) {
        this.shortLink = shortLink;
    }

    public String getPreviewLink() {
        return previewLink;
    }

    public void setPreviewLink(String previewLink) {
        this.previewLink = previewLink;
    }
}
