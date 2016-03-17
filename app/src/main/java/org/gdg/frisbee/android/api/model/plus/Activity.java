package org.gdg.frisbee.android.api.model.plus;

import org.gdg.frisbee.android.api.deserializer.ZuluDateTimeDeserializer;
import org.joda.time.DateTime;

import java.util.List;

public class Activity {
    String id;
    String published;
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
        return ZuluDateTimeDeserializer.DATE_TIME_FORMATTER.parseDateTime(published);
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
