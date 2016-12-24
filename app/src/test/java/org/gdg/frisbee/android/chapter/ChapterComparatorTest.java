package org.gdg.frisbee.android.chapter;

import android.location.Location;
import android.location.LocationManager;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Geo;
import org.gdg.frisbee.android.app.BaseApp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, application = BaseApp.class)
public class ChapterComparatorTest {

    private Location locationIstanbul;

    private static final Chapter chapterWithoutLocation = new Chapter("GDG AAUA", "115737274430015604080");
    private static final Chapter chapterStartsWithLetterA = new Chapter("GDG AAUA", "115737274430015604080");
    private static final Chapter chapterStartsWithDigit = new Chapter("GDG 6th October", "105609152381762460369");
    private static final Chapter chapterIstanbul = new Chapter("GDG Istanbul", "100514812580249787371");
    private static final Chapter chapterWithDoubleSpaceInName = new Chapter("GDG  Mukono", "108217555392256442794");

    @Before
    public void setup() {
        locationIstanbul = new Location(LocationManager.GPS_PROVIDER);
        locationIstanbul.setLatitude(28.97702);
        locationIstanbul.setLongitude(41.011511);

        chapterStartsWithLetterA.setGeo(new Geo(7.083333, 4.833333));
        chapterStartsWithDigit.setGeo(new Geo(29.938126, 30.91398));
        chapterIstanbul.setGeo(new Geo(28.97696, 41.00527));
    }

    @Test
    public void shouldBeAlphabeticalWhenNoHomeAndLocation() {
        ChapterComparator comparator = new ChapterComparator(null, null);

        assertElementsOrderedLikeThis(comparator,
                chapterStartsWithDigit,
                chapterStartsWithLetterA,
                chapterIstanbul);
    }

    @Test
    public void doubleSpaceInNameShouldNotBeFirst() {
        ChapterComparator comparator = new ChapterComparator(null, null);

        assertElementsOrderedLikeThis(comparator,
                chapterStartsWithDigit,
                chapterStartsWithLetterA,
                chapterWithDoubleSpaceInName);
    }

    @Test
    public void nearestLocationShouldBeTop() {
        ChapterComparator comparator = new ChapterComparator(null, locationIstanbul);

        assertElementsOrderedLikeThis(comparator,
                chapterIstanbul,
                chapterStartsWithDigit,
                chapterStartsWithLetterA);
    }

    @Test
    public void chapterWithoutLocationShouldBeSortedAlphabetically() {
        ChapterComparator comparator = new ChapterComparator(null, locationIstanbul);

        assertElementsOrderedLikeThis(comparator,
                chapterIstanbul,
                chapterStartsWithDigit,
                chapterWithoutLocation);
    }

    @Test
    public void homeChapterShouldBeTop() {
        ChapterComparator comparator =
                new ChapterComparator(chapterIstanbul, locationIstanbul);

        assertElementsOrderedLikeThis(comparator,
                chapterIstanbul, chapterWithoutLocation);
        assertElementsOrderedLikeThis(comparator,
                chapterIstanbul, chapterStartsWithDigit);
        assertElementsOrderedLikeThis(comparator,
                chapterIstanbul, chapterStartsWithLetterA);
        assertElementsOrderedLikeThis(comparator,
                chapterIstanbul, chapterWithDoubleSpaceInName);
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
