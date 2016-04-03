package org.gdg.frisbee.android.api.model.plus;

import java.util.List;

public class PlusObject {
    String content;
    Actor actor;
    List<Attachment> attachments;

    public String getContent() {
        return content;
    }

    public Actor getActor() {
        return actor;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }
}
