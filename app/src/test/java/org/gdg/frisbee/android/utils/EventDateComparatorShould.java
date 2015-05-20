package org.gdg.frisbee.android.utils;

import org.gdg.frisbee.android.adapter.EventAdapter;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.gdg.frisbee.android.eventseries.EventDateComparator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventDateComparatorShould {

    private EventDateComparator comparator;
    private EventAdapter.Item eventInMarch;
    private EventAdapter.Item eventInApril;
    private EventAdapter.Item eventWithoutStartDate;

    @Before
    public void setup() {
        comparator = new EventDateComparator();
        SimpleEvent simpleEventInMarch = new Event() {
            @Override
            public DateTime getStart() {
                return new DateTime(2014, 3, 1, 0, 0);
            }
        };
        eventInMarch = new EventAdapter.Item(simpleEventInMarch);

        SimpleEvent simpleEventInApril = new Event() {
            @Override
            public DateTime getStart() {
                return new DateTime(2014, 4, 1, 0, 0);
            }
        };
        eventInApril = new EventAdapter.Item(simpleEventInApril);

        SimpleEvent simpleEventWithoutStartDate = new Event() {
            @Override
            public DateTime getStart() {
                return null;
            }
        };
        eventWithoutStartDate = new EventAdapter.Item(simpleEventWithoutStartDate);
    }

    @Test
    public void valueAnEventHigherThanAnEventThatStartedBefore() {
        assertEquals(1, comparator.compare(eventInApril, eventInMarch));

        assertEquals(-1, comparator.compare(eventInMarch, eventInApril));
        assertEquals(0, comparator.compare(eventInMarch, eventInMarch));
    }

    @Test
    public void valueAnEventLowerThanAnEventWithoutStartDate() {
        assertEquals(-1, comparator.compare(eventInApril, eventWithoutStartDate));

        assertEquals(1, comparator.compare(eventWithoutStartDate, eventInApril));
        assertEquals(0, comparator.compare(eventWithoutStartDate, eventWithoutStartDate));
    }
}
