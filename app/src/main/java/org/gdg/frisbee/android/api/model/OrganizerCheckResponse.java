package org.gdg.frisbee.android.api.model;

import java.util.ArrayList;

public class OrganizerCheckResponse {
    private String msg, user;
    private ArrayList<Chapter> chapters;

    public OrganizerCheckResponse() {
        chapters = new ArrayList<>();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ArrayList<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(ArrayList<Chapter> chapters) {
        this.chapters = chapters;
    }

    class Chapter {
        private String id, name;

        String getName() {
            return name;
        }

        void setName(String name) {
            this.name = name;
        }

        String getId() {
            return id;
        }

        void setId(String id) {
            this.id = id;
        }
    }
}
