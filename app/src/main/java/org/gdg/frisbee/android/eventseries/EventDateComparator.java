package org.gdg.frisbee.android.eventseries;

import android.support.annotation.NonNull;

import java.util.Comparator;

class EventDateComparator implements Comparator<EventAdapter.Item> {

    public EventDateComparator() {
    }

    @Override
    public int compare(@NonNull EventAdapter.Item event1, @NonNull EventAdapter.Item event2) {
        if (event1.getEvent().getStart() == null) {
            if (event2.getEvent().getStart() == null) {
                return 0;
            } else {
                return 1;
            }
        } else if (event2.getEvent().getStart() == null) {
            return -1;
        } else {
            return event1.getEvent().getStart().compareTo(event2.getEvent().getStart());
        }
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
