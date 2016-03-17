package org.gdg.frisbee.android.api.model.plus;

import java.util.List;

public class Person {
    private String url;
    private String tagline;
    private String aboutMe;
    private Image image;
    private Cover cover;
    private List<Urls> urls;
    private String displayName;

    public String getUrl() {
        return url;
    }

    public String getTagline() {
        return tagline;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public Image getImage() {
        return image;
    }

    public Cover getCover() {
        return cover;
    }

    public List<Urls> getUrls() {
        return urls;
    }

    public String getDisplayName() {
        return displayName;
    }
}
