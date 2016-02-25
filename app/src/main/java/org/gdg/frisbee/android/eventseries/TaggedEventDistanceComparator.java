package org.gdg.frisbee.android.eventseries;

import android.location.Location;

import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.app.App;

import java.util.Comparator;

class TaggedEventDistanceComparator implements Comparator<EventAdapter.Item> {

    public TaggedEventDistanceComparator() {
    }

    @Override
    public int compare(EventAdapter.Item eventItem1, EventAdapter.Item eventItem2) {
        float[] results = new float[1];
        float[] results2 = new float[1];

        Event event1 = (Event) eventItem1.getEvent();
        Event event2 = (Event) eventItem2.getEvent();

        Location lastLocation = App.getInstance().getLastLocation();
        if (lastLocation == null) {
            return event1.getStart().compareTo(event2.getStart());
        }

        if (event1.getLatLng() == null) {
            return 1;
        }
        if (event2.getLatLng() == null) {
            return -1;
        }

        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
            event1.getLatLng().getLat(), event1.getLatLng().getLng(), results);
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
            event2.getLatLng().getLat(), event2.getLatLng().getLng(), results2);

        if (results[0] == results2[0]) {
            return 0;
        } else if (results[0] > results2[0]) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
