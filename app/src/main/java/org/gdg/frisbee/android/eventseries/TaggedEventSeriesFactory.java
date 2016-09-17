package org.gdg.frisbee.android.eventseries;

import android.content.Context;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;

public final class TaggedEventSeriesFactory {

    private static final DateTime START_TIME_DEVFEST = new DateTime(2016, 9, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTime START_TIME_WTM = new DateTime(2017, 2, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTime START_TIME_STUDY_JAMS = new DateTime(2017, 1, 15, 0, 0, DateTimeZone.UTC);
    private static final DateTime START_TIME_IOEXTENDED = new DateTime(2017, 5, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTime START_TIME_GCP_NEXT = new DateTime(2017, 3, 15, 0, 0, DateTimeZone.UTC);
    private static final DateTime END_TIME_DEVFEST = new DateTime(2017, 1, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTime END_TIME_WTM = new DateTime(2017, 4, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTime END_TIME_STUDY_JAMS = new DateTime(2017, 5, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTime END_TIME_IOEXTENDED = new DateTime(2017, 6, 1, 0, 0, DateTimeZone.UTC);
    private static final DateTime END_TIME_GCP_NEXT = new DateTime(2017, 4, 15, 0, 0, DateTimeZone.UTC);

    public static List<TaggedEventSeries> createAvailableEventSeries(Context context) {

        List<TaggedEventSeries> series = new ArrayList<>();
        //Add DevFest
        addTaggedEventSeriesIfDateFits(series, new TaggedEventSeries(context,
            R.style.Theme_GDG_Special_DevFest,
            "devfest",
            GdgNavDrawerActivity.DRAWER_DEVFEST,
            START_TIME_DEVFEST,
            END_TIME_DEVFEST));
        //Add Women Techmakers
        addTaggedEventSeriesIfDateFits(series, new TaggedEventSeries(context,
            R.style.Theme_GDG_Special_Wtm,
            "wtm",
            GdgNavDrawerActivity.DRAWER_WTM,
            START_TIME_WTM,
            END_TIME_WTM));
        //Add Android Fundamentals Study Jams
        addTaggedEventSeriesIfDateFits(series, new TaggedEventSeries(context,
            R.style.Theme_GDG_Special_StudyJams,
            "studyjam",
            GdgNavDrawerActivity.DRAWER_STUDY_JAM,
            START_TIME_STUDY_JAMS,
            END_TIME_STUDY_JAMS));
        //Add IO Extended
        addTaggedEventSeriesIfDateFits(series, new TaggedEventSeries(context,
            R.style.Theme_GDG_Special_IOExtended,
            "i-oextended",
            GdgNavDrawerActivity.DRAWER_IO_EXTENDED,
            START_TIME_IOEXTENDED,
            END_TIME_IOEXTENDED));
        //Add GCP NEXT
        addTaggedEventSeriesIfDateFits(series, new TaggedEventSeries(context,
            R.style.Theme_GDG_Special_GCPNEXT,
            "gcpnext",
            GdgNavDrawerActivity.DRAWER_GCP_NEXT,
            START_TIME_GCP_NEXT,
            END_TIME_GCP_NEXT));

        return series;
    }

    private static void addTaggedEventSeriesIfDateFits(List<TaggedEventSeries> list,
                                                       TaggedEventSeries taggedEventSeries) {
        DateTime now = DateTime.now();
        if (BuildConfig.DEBUG || (now.isAfter(taggedEventSeries.getStartDate())
            && now.isBefore(taggedEventSeries.getEndDate()))) {
            list.add(taggedEventSeries);
        }
    }

    private TaggedEventSeriesFactory() {
        //no instance
    }
}
