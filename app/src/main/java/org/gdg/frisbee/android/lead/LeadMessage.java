package org.gdg.frisbee.android.lead;

import android.support.annotation.StringRes;

import org.gdg.frisbee.android.R;

public class LeadMessage {
    private final String mTitle;
    private final Type mType;
    private final String mDetails;
    private String mLinkUrl;

    private LeadMessage(Type type, final String title, final String details) {
        this(type, title, details, null);
    }

    private LeadMessage(final Type type, final String title, String details, final String linkUrl) {
        mTitle = title;
        mDetails = details;
        mType = type;
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
        message(R.string.message),
        resource(R.string.resource);

        @StringRes
        private final int mName;

        Type(@StringRes final int name) {
            mName = name;
        }

        @StringRes
        public int getName() {
            return mName;
        }
    }
}
