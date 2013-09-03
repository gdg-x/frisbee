package org.gdg.frisbee.android.api.model;

import org.joda.time.DateTime;

public interface SimpleEvent {

    DateTime getStart();

    DateTime getEnd();

    String getTitle();

    String getIconUrl();

    String getId();

    String getGPlusEventLink();

    String getLink();

    String getLocation();
}
