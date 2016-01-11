package org.gdg.frisbee.android.arrow;

import com.google.android.gms.plus.model.people.Person;

public class Organizer {
    private String plusId;
    private String chapterId;
    private String chapterName;
    private Person resolved;

    public String getPlusId() {
        return plusId;
    }

    public void setPlusId(String plusId) {
        this.plusId = plusId;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    public String getChapterName() {
        return chapterName;
    }

    public void setChapterName(String chapterName) {
        this.chapterName = chapterName;
    }

    public Person getResolved() {
        return resolved;
    }

    public void setResolved(Person resolved) {
        this.resolved = resolved;
    }
}
