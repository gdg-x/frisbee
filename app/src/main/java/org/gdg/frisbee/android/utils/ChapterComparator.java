package org.gdg.frisbee.android.utils;

import android.content.SharedPreferences;
import android.location.Location;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.app.App;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 24.07.13
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */
public class ChapterComparator implements Comparator<Chapter> {

    private SharedPreferences mPreferences;

    public ChapterComparator(SharedPreferences prefs) {
        mPreferences = prefs;
    }

    @Override
    public int compare(Chapter chapter, Chapter chapter2) {
        float[] results = new float[1];
        float[] results2 = new float[1];

        if(chapter.getGplusId().equals(mPreferences.getString(Const.SETTINGS_HOME_GDG,""))) {
            return -1;
        }

        if(chapter2.getGplusId().equals(mPreferences.getString(Const.SETTINGS_HOME_GDG,""))) {
            return 1;
        }

        Location lastLocation = App.getInstance().getLastLocation();
        if(lastLocation == null)
            return chapter.getName().compareTo(chapter2.getName());

        if(chapter.getGeo() == null)
            return 1;
        if(chapter2.getGeo() == null)
            return -1;

        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), chapter.getGeo().getLat(), chapter.getGeo().getLng(), results);
        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), chapter2.getGeo().getLat(), chapter2.getGeo().getLng(), results2);

        if(results[0] == results2[0])
            return 0;
        else if(results[0] > results2[0])
            return 1;
        else
            return -1;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
