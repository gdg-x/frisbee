package org.gdg.frisbee.android.widget;

import android.content.Intent;
import android.net.Uri;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;

import timber.log.Timber;

public class GdgDashClockExtension extends DashClockExtension {

    private ArrayList<Chapter> mChapters;

    private Chapter findChapter(String chapterId) {
        if (chapterId != null) {
            for (Chapter chapter : mChapters) {
                if (chapter.getGplusId().equals(chapterId)) {
                    return chapter;
                }
            }
        }
        return null;
    }

    @Override
    protected void onUpdateData(int i) {

        App.getInstance().getModelCache().getAsync("chapter_list_hub", false, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                mChapters = ((Directory) item).getGroups();
                final Chapter homeGdg = findChapter(PrefUtils.getHomeChapterId(GdgDashClockExtension.this));

                if (homeGdg == null) {
                    Timber.d("Got no Home GDG");
                } else {
                    Timber.d("Fetching events");
                    GroupDirectory.getChapterEventList(
                            new DateTime(), 
                            new DateTime().plusMonths(1), 
                            homeGdg.getGplusId(), 
                            new Response.Listener<ArrayList<Event>>() {
                                @Override
                                public void onResponse(ArrayList<Event> events) {

                                    if (events.size() > 0) {
                                        if (events.get(0).getGPlusEventLink() != null) {

                                            Event event = events.get(0);

                                            String expandedBody =
                                                    event.getStart()
                                                            .toLocalDateTime()
                                                            .toString(DateTimeFormat.patternForStyle("MS",
                                                                            getResources().getConfiguration().locale)
                                                    );
                                            publishUpdate(new ExtensionData()
                                                    .visible(true)
                                                    .icon(R.drawable.ic_dashclock)
                                                    .status("GDG")
                                                    .expandedTitle(event.getTitle())
                                                    .expandedBody(expandedBody)
                                                    .clickIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(event.getGPlusEventLink()))));
                                        }
                                    } else {
                                        publishUpdate(new ExtensionData()
                                                .visible(false));
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Timber.e("Error updating Widget", volleyError);
                                }
                            }

                    ).execute();
                }
            }

            @Override
            public void onNotFound(String key) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

}
