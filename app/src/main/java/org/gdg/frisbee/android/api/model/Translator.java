package org.gdg.frisbee.android.api.model;

public class Translator extends Contributor {
    public Translator(String name, String login, String avatarUrl, int contributions) {
        super();
        setName(name);
        setLogin(login);
        setHtmlUrl("https://crowdin.com/profile/" + login);
        setAvatarUrl(avatarUrl);
        setContributions(contributions);
    }
}
