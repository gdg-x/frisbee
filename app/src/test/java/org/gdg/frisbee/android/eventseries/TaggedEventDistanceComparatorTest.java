package org.gdg.frisbee.android.eventseries;

import android.location.Location;

import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.eventseries.EventAdapter.Item;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class TaggedEventDistanceComparatorTest {

    @Mock Event eventInMarch;
    @Mock Event eventInApril;
    @Mock Event eventInDecember;

    private Location location;

    @Before
    public void setup() {
        initMocks(this);
        location = new Location("mock");
        location.setLatitude(41d);
        location.setLongitude(29d);

        when(eventInMarch.getStart()).thenReturn(new DateTime(2014, 3, 1, 0, 0));
        when(eventInApril.getStart()).thenReturn(new DateTime(2014, 4, 1, 0, 0));
        when(eventInDecember.getStart()).thenReturn(new DateTime(2014, 12, 1, 0, 0));
    }

    @Test
    public void givenUserHasNoLocation_thenComparatorShouldSortUsingDates() {
        TaggedEventDistanceComparator comparator = new TaggedEventDistanceComparator(null);

        Item eventInMarch =  new Item(this.eventInMarch);
        Item eventInApril = new Item(this.eventInApril);
        Item eventInDecember = new Item(this.eventInDecember);

        assertElementsOrderedLikeThis(comparator, eventInMarch, eventInApril, eventInDecember);
    }

    @Test
    public void givenEventsWithNoLocation_thenComparatorShouldSortUsingDates() {
        TaggedEventDistanceComparator comparator = new TaggedEventDistanceComparator(location);

        Item eventInMarch =  new Item(this.eventInMarch);
        Item eventInApril = new Item(this.eventInApril);
        Item eventInDecember = new Item(this.eventInDecember);

        assertElementsOrderedLikeThis(comparator, eventInMarch, eventInApril, eventInDecember);
    }

    @Test
    public void givenEventWithNoLocation_thenWithLocationShouldComeFirst() {
        Item eventWithLocation = givenEventWithLocation(41, 29);
        Item eventInMarch =  new Item(this.eventInMarch);

        TaggedEventDistanceComparator comparator = new TaggedEventDistanceComparator(location);

        assertElementsOrderedLikeThis(comparator, eventWithLocation, eventInMarch);
    }

    @Test
    public void shouldSortUsingLocation() {
        Item eventWithMyLocation = givenEventWithLocation(41, 29);
        Item eventWithFurtherLocation =  givenEventWithLocation(42d, 30d);

        TaggedEventDistanceComparator comparator = new TaggedEventDistanceComparator(location);

        assertElementsOrderedLikeThis(comparator, eventWithMyLocation, eventWithFurtherLocation);
    }

    private static Item givenEventWithLocation(double lat, double lng) {
        Event eventWithLocation = mock(Event.class);
        when(eventWithLocation.getLatLng()).thenReturn(new Event.LatLng(lat, lng));
        return new Item(eventWithLocation);
    }

    private static void assertElementsOrderedLikeThis(Comparator<? super Item> comparator, Item... elements) {
        List<Item> expectedOrder = Arrays.asList(elements);

        List<Item> shuffledAndSorted = new ArrayList<>(expectedOrder);
        Collections.shuffle(shuffledAndSorted, new Random(0));
        Collections.sort(shuffledAndSorted, comparator);
        assertEquals(expectedOrder, shuffledAndSorted);

        List<Item> reversedAndSorted = new ArrayList<>(expectedOrder);
        Collections.reverse(reversedAndSorted);
        Collections.sort(reversedAndSorted, comparator);
        assertEquals(expectedOrder, reversedAndSorted);
    }
}
