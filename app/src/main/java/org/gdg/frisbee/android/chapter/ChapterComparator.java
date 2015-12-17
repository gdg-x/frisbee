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
        float[] results = new float[1];
        float[] results2 = new float[1];

        if (chapter.getGplusId().equals(homeChapterId)) {
            return -1;
        }

        if (chapter2.getGplusId().equals(homeChapterId)) {
            return 1;
        }

        if (lastLocation == null) {
            return chapter.getName().compareTo(chapter2.getName());
        }

        if (chapter.getGeo() == null) {
            return 1;
        }
        if (chapter2.getGeo() == null) {
            return -1;
        }

        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                chapter.getGeo().getLat(), chapter.getGeo().getLng(), results);
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                chapter2.getGeo().getLat(), chapter2.getGeo().getLng(), results2);

        final boolean closeEnough = results[0] <= MAX_DISTANCE;
        final boolean closeEnough2 = results2[0] <= MAX_DISTANCE;
        
        if (closeEnough && closeEnough2) {
            if (results[0] == results2[0]) {
                return 0;
            } else if (results[0] > results2[0]) {
                return 1;
            } else {
                return -1;
            }
        } else if (closeEnough) {
            return -1;
        } else if (closeEnough2) {
            return 1;
        } else {
            return chapter.getName().compareTo(chapter2.getName());
        }
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
