package org.gdg.frisbee.android.api.model;

public class FirebaseDynamicLinksRequest {
    private String longDynamicLink;
    private Suffix suffix;

    public FirebaseDynamicLinksRequest(String longDynamicLink, Suffix suffix) {
        this.longDynamicLink = longDynamicLink;
        this.suffix = suffix;
    }
}
