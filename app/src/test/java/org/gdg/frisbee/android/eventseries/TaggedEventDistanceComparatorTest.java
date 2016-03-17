package org.gdg.frisbee.android.eventseries;

import android.location.Location;

import org.gdg.frisbee.android.api.model.Event;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TaggedEventDistanceComparatorTest {

    @Mock
    Event eventInMarch;
    @Mock
    Event eventInApril;
    @Mock
    Event eventWithLocation;
    @Mock
    Location location;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void givenEventsWithNoLocation_thenComparatorShouldSortUsingDates() {
        when(eventInMarch.getStart()).thenReturn(new DateTime(2014, 3, 1, 0, 0));
        when(eventInApril.getStart()).thenReturn(new DateTime(2014, 4, 1, 0, 0));
        when(eventInMarch.getLatLng()).thenReturn(null);
        when(eventInApril.getLatLng()).thenReturn(null);
        when(eventWithLocation.getLatLng()).thenReturn(new Event.LatLng(41, 29));
        when(location.getLatitude()).thenReturn(41d);
        when(location.getLongitude()).thenReturn(29d);

        TaggedEventDistanceComparator comparator = new TaggedEventDistanceComparator(location);

        EventAdapter.Item eventInMarch =  new EventAdapter.Item(this.eventInMarch);
        EventAdapter.Item eventInApril = new EventAdapter.Item(this.eventInApril);
        EventAdapter.Item eventWithLocation = new EventAdapter.Item(this.eventWithLocation);

        assertElementsOrderedLikeThis(comparator, eventInMarch, eventInApril, eventWithLocation, eventInApril);
    }


    private <T> void assertElementsOrderedLikeThis(
        Comparator<? super T> comparator, T... elements) {
        List<T> expectedOrder = Arrays.asList(elements);

        List<T> shuffledAndSorted = new ArrayList<>(expectedOrder);
        Collections.shuffle(shuffledAndSorted, new Random(0));
        Collections.sort(shuffledAndSorted, comparator);
        assertEquals(expectedOrder, shuffledAndSorted);

        List<T> reversedAndSorted = new ArrayList<>(expectedOrder);
        Collections.reverse(reversedAndSorted);
        Collections.sort(reversedAndSorted, comparator);
        assertEquals(expectedOrder, reversedAndSorted);
    }
}
