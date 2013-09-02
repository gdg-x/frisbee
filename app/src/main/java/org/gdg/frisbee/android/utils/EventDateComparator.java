package org.gdg.frisbee.android.utils;

import java.util.Comparator;

import org.gdg.frisbee.android.adapter.EventAdapter;

public class EventDateComparator implements Comparator<EventAdapter.Item> {

    public EventDateComparator() {
    }

    @Override
    public int compare(EventAdapter.Item event1, EventAdapter.Item event2) {
        if (event1.getEvent().getStart() == null){
            return 1;
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
