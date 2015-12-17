package org.gdg.frisbee.android.api.model;

public class Contributor implements GdgPerson {
    private String name, login, avatarUrl, htmlUrl;
    private int contributions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }


    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public int getContributions() {
        return contributions;
    }

    public void setContributions(int contributions) {
        this.contributions = contributions;
    }

    @Override
    public String getImageUrl() {
        return avatarUrl;
    }

    @Override
    public String getUrl() {
        return htmlUrl;
    }

    @Override
    public String getPrimaryText() {
        return login;
    }

    @Override
    public String getSecondaryText() {
        return null;
    }
}
