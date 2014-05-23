package org.gdg.frisbee.android.api.model;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TaggedEvent implements SimpleEvent {
    String eventUrl, title, location, timezone;
    String _id;
    LatLng geo;
    DateTime end, start;

    @Override
    public DateTime getStart() {
        return convertToEventTimezone(start);
    }

    @Override
    public DateTime getEnd() {
        return convertToEventTimezone(end);
    }

    private DateTime convertToEventTimezone(DateTime dateTime) {
        dateTime = dateTime.withZoneRetainFields(DateTimeZone.UTC);
        return dateTime.withZone(DateTimeZone.forID(timezone));
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getIconUrl() {
        return null;
    }

    @Override
    public String getId() {
       return _id;
    }

    @Override
    public String getGPlusEventLink() {
        return eventUrl;
    }

    @Override
    public String getLink() {
        return eventUrl;
    }

    public String getLocation(){
        return location;
    }

    public LatLng getLatLng() {
        return geo;
    }

    public static class LatLng {
        double lat, lng;

        public double getLng() {
            return lng;
        }

        public double getLat() {
            return lat;
        }
    }
}
