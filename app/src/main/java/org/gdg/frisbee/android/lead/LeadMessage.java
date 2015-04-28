package org.gdg.frisbee.android.lead;

public class LeadMessage {
    private final String mTitle;
    private final Type mType;
    private final String mDetails;
    private String mLinkUrl;

    private LeadMessage(Type type, final String title, final String details) {
        mTitle = title;
        mDetails = details;
        mType = type;
    }

    private LeadMessage(final Type type, final String title, String details, final String linkUrl) {
        this(type, title, details);
        mLinkUrl = linkUrl;
    }

    public static LeadMessage newMessage(final String title, final String details) {
        return new LeadMessage(Type.message, title, details);
    }

    public static LeadMessage newResource(final String title, final String details, final String linkUrl) {
        return new LeadMessage(Type.resource, title, details, linkUrl);
    }

    public String getTitle() {
        return mTitle;
    }

    public Type getType() {
        return mType;
    }

    public String getLinkUrl() {
        return mLinkUrl;
    }

    public String getDetails() {
        return mDetails;
    }

    public enum Type {
        message(),
        resource()
    }
}
