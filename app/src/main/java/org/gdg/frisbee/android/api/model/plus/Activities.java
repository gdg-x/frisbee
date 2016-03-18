package org.gdg.frisbee.android.api.model.plus;

import java.util.List;

public class Activities {
    String nextPageToken;
    List<Activity> items;

    public String getNextPageToken() {
        return nextPageToken;
    }

    public List<Activity> getItems() {
        return items;
    }
}
