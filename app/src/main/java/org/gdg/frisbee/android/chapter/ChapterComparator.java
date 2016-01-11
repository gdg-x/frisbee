package org.gdg.frisbee.android.chapter;

import android.location.Location;
import android.support.annotation.Nullable;

import org.gdg.frisbee.android.api.model.Chapter;

import java.util.Comparator;

public class ChapterComparator implements Comparator<Chapter> {

    private static final float MAX_DISTANCE = 500000;
    private final String homeChapterId;
    private final Location lastLocation;

    public ChapterComparator(@Nullable String homeChapterId, @Nullable Location lastLocation) {
        this.homeChapterId = homeChapterId;
        this.lastLocation = lastLocation;
    }

    @Override
    public int compare(Chapter chapter, Chapter chapter2) {

        if (chapter.getGplusId().equals(homeChapterId)) {
            return -1;
        }

        if (chapter2.getGplusId().equals(homeChapterId)) {
            return 1;
        }

        if (lastLocation == null || (chapter.getGeo() == null && chapter2.getGeo() == null)) {
            return chapter.compareTo(chapter2);
        }

        final boolean closeEnough;
        final boolean closeEnough2;
        final float[] results = new float[1];
        final float[] results2 = new float[1];

        if (chapter.getGeo() != null) {
            Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                    chapter.getGeo().getLat(), chapter.getGeo().getLng(), results);
            closeEnough = results[0] <= MAX_DISTANCE;
        } else {
            closeEnough = false;
        }

        if (chapter2.getGeo() != null) {
            Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                    chapter2.getGeo().getLat(), chapter2.getGeo().getLng(), results2);
            closeEnough2 = results2[0] <= MAX_DISTANCE;
        } else {
            closeEnough2 = false;
        }

        if (closeEnough && closeEnough2) {
            return integerCompare((int) results[0], (int) results2[0]);
        } else if (closeEnough) {
            return -1;
        } else if (closeEnough2) {
            return 1;
        } else {
            return chapter.compareTo(chapter2);
        }
    }

    public static int integerCompare(int lhs, int rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
