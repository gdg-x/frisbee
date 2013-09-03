package org.gdg.frisbee.android.api.model;


import org.joda.time.DateTime;

public class TaggedEvent implements SimpleEvent {
    String gplusEventUrl, name, defaultEventUrl, location;
    LatLng latlng;
    long end, start;

    @Override
    public DateTime getStart() {
        return new DateTime(start * 1000);
    }

    @Override
    public DateTime getEnd() {
        return new DateTime(end * 1000);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getIconUrl() {
        return null;
    }

    @Override
    public String getId() {
        String[] parts = defaultEventUrl.split("/");
        return parts[1];
    }

    @Override
    public String getGPlusEventLink() {
        return gplusEventUrl;
    }

    @Override
    public String getLink() {
        return defaultEventUrl;
    }

    public String getLocation(){
        return location;
    }

    public LatLng getLatLng() {
        return latlng;
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
