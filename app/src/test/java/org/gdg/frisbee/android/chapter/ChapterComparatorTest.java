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
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, application = BaseApp.class)
public class ChapterComparatorTest {

    private Location locationIstanbul;

    private static final Chapter chapterWithoutLocation = new Chapter("GDG AAUA", "115737274430015604080");
    private static final Chapter chapterStartsWithLetterA = new Chapter("GDG AAUA", "115737274430015604080");
    private static final Chapter chapterStartsWithDigit = new Chapter("GDG 6th October", "105609152381762460369");
    private static final Chapter chapterIstanbul = new Chapter("GDG Istanbul", "100514812580249787371");
    private static final Chapter chapterEskisehir = new Chapter("GDG Eskişehir", "115602346615910585045");
    private static final Chapter chapterWithDoubleSpaceInName = new Chapter("GDG  Mukono", "108217555392256442794");

    @Before
    public void setup() {
        locationIstanbul = new Location(LocationManager.GPS_PROVIDER);
        locationIstanbul.setLatitude(28.97702);
        locationIstanbul.setLongitude(41.011511);

        chapterStartsWithLetterA.setGeo(new Geo(7.083333, 4.833333));
        chapterStartsWithDigit.setGeo(new Geo(29.938126, 30.91398));
        chapterIstanbul.setGeo(new Geo(28.97696, 41.00527));
        chapterEskisehir.setGeo(new Geo(39.7766667, 30.5205556));
    }

    @Test
    public void shouldBeAlphabeticalWhenNoHomeAndLocation() {
        ChapterComparator comparator = new ChapterComparator(null, null);

        //Chapter starting with digit should come before chapter starting with 'A'
        assertTrue(0 > comparator.compare(chapterStartsWithDigit, chapterStartsWithLetterA));
        assertTrue(0 < comparator.compare(chapterStartsWithLetterA, chapterStartsWithDigit));
        assertTrue(0 == comparator.compare(chapterStartsWithDigit, chapterStartsWithDigit));

        //Chapter starting with digit should come before Istanbul
        assertTrue(0 > comparator.compare(chapterStartsWithDigit, chapterIstanbul));
        assertTrue(0 < comparator.compare(chapterIstanbul, chapterStartsWithDigit));

    }

    @Test
    public void doubleSpaceInNameShouldNotBeFirst() {
        ChapterComparator comparator = new ChapterComparator(null, null);

        assertTrue(0 > comparator.compare(chapterStartsWithDigit, chapterWithDoubleSpaceInName));
        assertTrue(0 < comparator.compare(chapterWithDoubleSpaceInName, chapterStartsWithDigit));
    }

    @Test
    public void nearestLocationShouldBeTop() {
        ChapterComparator comparator = new ChapterComparator(null, locationIstanbul);

        //Chapter starting with digit should come before chapter starting with 'A'
        assertTrue(0 > comparator.compare(chapterStartsWithDigit, chapterStartsWithLetterA));
        assertTrue(0 < comparator.compare(chapterStartsWithLetterA, chapterStartsWithDigit));
        assertTrue(0 == comparator.compare(chapterStartsWithDigit, chapterStartsWithDigit));

        //Istanbul should be top because it is geographically close to user's location.
        assertTrue(0 > comparator.compare(chapterIstanbul, chapterStartsWithDigit));
        assertTrue(0 < comparator.compare(chapterStartsWithDigit, chapterIstanbul));

        //Istanbul should be top because it is geographically close to user's location.
        assertTrue(0 > comparator.compare(chapterIstanbul, chapterEskisehir));
        assertTrue(0 < comparator.compare(chapterEskisehir, chapterIstanbul));
    }

    @Test
    public void chapterWithoutLocationShouldBeSortedAlphabetically() {
        ChapterComparator comparator = new ChapterComparator(null, locationIstanbul);

        //Chapter starting with digit should come before chapter starting with 'A'
        assertTrue(0 > comparator.compare(chapterStartsWithDigit, chapterWithoutLocation));
        assertTrue(0 < comparator.compare(chapterWithoutLocation, chapterStartsWithDigit));

        //Istanbul should be top because it is geographically close to user's location.
        assertTrue(0 > comparator.compare(chapterIstanbul, chapterWithoutLocation));
        assertTrue(0 < comparator.compare(chapterWithoutLocation, chapterIstanbul));
    }

    @Test
    public void homeChapterShouldBeTop() {
        ChapterComparator comparator = new ChapterComparator(chapterIstanbul.getGplusId(), locationIstanbul);

        assertTrue(0 > comparator.compare(chapterIstanbul, chapterWithoutLocation));
        assertTrue(0 < comparator.compare(chapterWithoutLocation, chapterIstanbul));

        assertTrue(0 > comparator.compare(chapterIstanbul, chapterStartsWithDigit));
        assertTrue(0 < comparator.compare(chapterStartsWithDigit, chapterIstanbul));

        assertTrue(0 > comparator.compare(chapterIstanbul, chapterStartsWithLetterA));
        assertTrue(0 < comparator.compare(chapterStartsWithLetterA, chapterIstanbul));

        assertTrue(0 > comparator.compare(chapterIstanbul, chapterEskisehir));
        assertTrue(0 < comparator.compare(chapterEskisehir, chapterIstanbul));

        assertTrue(0 > comparator.compare(chapterIstanbul, chapterWithDoubleSpaceInName));
        assertTrue(0 < comparator.compare(chapterWithDoubleSpaceInName, chapterIstanbul));

    }
}
