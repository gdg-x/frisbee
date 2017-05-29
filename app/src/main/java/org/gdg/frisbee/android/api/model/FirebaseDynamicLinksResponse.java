package org.gdg.frisbee.android.api.model;

public class FirebaseDynamicLinksResponse {
    private final String shortLink;
    private final String previewLink;

    public FirebaseDynamicLinksResponse(String shortLink, String previewLink) {
        this.shortLink = shortLink;
        this.previewLink = previewLink;
    }

    public String getShortLink() {
        return shortLink;
    }

    public String getPreviewLink() {
        return previewLink;
    }
}
