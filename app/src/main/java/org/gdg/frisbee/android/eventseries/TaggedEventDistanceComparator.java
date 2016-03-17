package org.gdg.frisbee.android.eventseries;

import android.location.Location;

import org.gdg.frisbee.android.api.model.Event;

import java.util.Comparator;

class TaggedEventDistanceComparator implements Comparator<EventAdapter.Item> {

    private final Location lastLocation;

    public TaggedEventDistanceComparator(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    @Override
    public int compare(EventAdapter.Item eventItem1, EventAdapter.Item eventItem2) {
        float[] results = new float[1];
        float[] results2 = new float[1];

        Event event1 = (Event) eventItem1.getEvent();
        Event event2 = (Event) eventItem2.getEvent();

        if (lastLocation == null) {
            return event1.getStart().compareTo(event2.getStart());
        }

        Event.LatLng latLng1 = event1.getLatLng();
        Event.LatLng latLng2 = event2.getLatLng();
        if (latLng1 == null && latLng2 == null) {
            return event1.getStart().compareTo(event2.getStart());
        }
        if (latLng1 == null) {
            return 1;
        }
        if (latLng2 == null) {
            return -1;
        }

        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
            latLng1.getLat(), latLng1.getLng(), results);
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
            latLng2.getLat(), latLng2.getLng(), results2);

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
