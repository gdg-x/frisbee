package org.gdg.frisbee.android.utils;

import android.location.Location;

import java.util.Comparator;

import org.gdg.frisbee.android.adapter.EventAdapter;
import org.gdg.frisbee.android.api.model.TaggedEvent;
import org.gdg.frisbee.android.app.App;

public class TaggedEventDistanceComparator implements Comparator<EventAdapter.Item> {

    public TaggedEventDistanceComparator() {
    }

    @Override
    public int compare(EventAdapter.Item eventItem1, EventAdapter.Item eventItem2) {
        float[] results = new float[1];
        float[] results2 = new float[1];


        TaggedEvent event1 = (TaggedEvent) eventItem1.getEvent();
        TaggedEvent event2 = (TaggedEvent) eventItem2.getEvent();

        Location lastLocation = App.getInstance().getLastLocation();
        if(lastLocation == null)
            return event1.getStart().compareTo(event2.getStart());

        if(event1.getLatLng() == null)
            return 1;
        if(event2.getLatLng() == null)
            return -1;

        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), event1.getLatLng().getLat(), event1.getLatLng().getLng(), results);
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), event2.getLatLng().getLat(), event2.getLatLng().getLng(), results2);

        if(results[0] == results2[0])
            return 0;
        else if(results[0] > results2[0])
            return 1;
        else
            return -1;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
