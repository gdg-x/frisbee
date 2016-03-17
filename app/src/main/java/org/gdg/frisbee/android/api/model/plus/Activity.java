package org.gdg.frisbee.android.api.model.plus;

import java.util.List;

public class Activity {
    String id;
    DateTime published;
    String url;
    String content;
    String verb;
    PlusObject object;
    List<Attachment> attachments;
    String annotation;

    public String getId() {
        return id;
    }

    public DateTime getPublished() {
        return published;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public String getVerb() {
        return verb;
    }

    public PlusObject getObject() {
        return object;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public String getAnnotation() {
        return annotation;
    }
}
