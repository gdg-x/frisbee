package org.gdg.frisbee.android.widget;

import android.content.Intent;
import android.net.Uri;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
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

        App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB, false, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                mChapters = ((Directory) item).getGroups();
                final Chapter homeGdg = findChapter(PrefUtils.getHomeChapterId(GdgDashClockExtension.this));

                if (homeGdg == null) {
                    Timber.d("Got no Home GDG");
                } else {
                    Timber.d("Fetching events");
                    App.getInstance().getGroupDirectory().getChapterEventList(
                            (int) (new DateTime().getMillis() / 1000),
                            (int) (new DateTime().plusMonths(1).getMillis() / 1000),
                            homeGdg.getGplusId(),
                            new Callback<ArrayList<Event>>() {
                                @Override
                                public void success(ArrayList<Event> events, retrofit.client.Response response) {
                                    if (events.size() > 0) {
                                        if (events.get(0).getGPlusEventLink() != null) {

                                            Event event = events.get(0);

                                            String expandedBody =
                                                    event.getStart()
                                                            .toLocalDateTime()
                                                            .toString(DateTimeFormat.patternForStyle("MS",
                                                                            getResources().getConfiguration().locale));
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

                                @Override
                                public void failure(RetrofitError error) {
                                    Timber.e(error, "Error updating Widget");
                                }
                            });
                }
            }

            @Override
            public void onNotFound(String key) {
            }
        });
    }

}
