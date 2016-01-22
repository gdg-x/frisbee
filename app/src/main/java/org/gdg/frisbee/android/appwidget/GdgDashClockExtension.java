package org.gdg.frisbee.android.appwidget;

import android.content.Intent;
import android.net.Uri;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.PagedList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

import timber.log.Timber;

public class GdgDashClockExtension extends DashClockExtension {

    @Override
    protected void onUpdateData(int i) {
        PrefUtils.setWidgetAdded(this);

        final String homeGdg = PrefUtils.getHomeChapterId(GdgDashClockExtension.this);

        if (homeGdg == null) {
            Timber.d("Got no Home GDG");
            return;
        }
        Timber.d("Fetching events");
        App.getInstance().getGdgXHub().getChapterEventList(homeGdg,
            new DateTime(),
            new DateTime().plusMonths(1))
            .enqueue(new Callback<PagedList<Event>>() {
                @Override
                public void success(PagedList<Event> eventsPagedList) {
                    List<Event> events = eventsPagedList.getItems();
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
            });
    }
}
