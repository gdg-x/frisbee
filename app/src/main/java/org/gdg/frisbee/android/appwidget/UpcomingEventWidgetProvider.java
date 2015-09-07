/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.appwidget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.event.EventActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;

import timber.log.Timber;

public class UpcomingEventWidgetProvider extends AppWidgetProvider {
    private static final int REQUEST_CODE_LAUNCH_FRISBEE = 1000;

    public static class UpdateService extends Service {

        private ArrayList<Chapter> mChapters;

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {

            ComponentName thisWidget = new ComponentName(this, UpcomingEventWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);

            // Build the widget update for today
            buildUpdate(this, manager, thisWidget);
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Nullable
        private Chapter findChapter(@Nullable String chapterId) {
            if (chapterId != null) {
                for (Chapter chapter : mChapters) {
                    if (chapter.getGplusId().equals(chapterId)) {
                        return chapter;
                    }
                }
            }
            return null;
        }

        public void buildUpdate(final Context context, final AppWidgetManager manager, final ComponentName thisWidget) {
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_upcoming_event);
            Intent mainIntent = new Intent(context, MainActivity.class);
            final PendingIntent pi = PendingIntent.getActivity(context, REQUEST_CODE_LAUNCH_FRISBEE, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.container, pi);

            App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    mChapters = ((Directory) item).getGroups();
                    final Chapter homeGdg = findChapter(PrefUtils.getHomeChapterId(UpdateService.this));

                    if (homeGdg == null) {
                        Timber.d("Got no Home GDG");
                        showErrorChild(views, R.string.loading_data_failed, context);

                    } else {
                        Timber.d("Fetching events");
                        String groupName = homeGdg.getShortName();
                        views.setTextViewText(R.id.groupName, groupName);
                        views.setTextViewText(R.id.groupName2, groupName);

                        fetchEvents(homeGdg, views, manager, thisWidget);
                    }
                }

                @Override
                public void onNotFound(String key) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });

        }

        private void fetchEvents(Chapter homeGdg, final RemoteViews views, final AppWidgetManager manager, final ComponentName thisWidget) {
            App.getInstance().getGroupDirectory()
                    .getChapterEventList(
                            (int) (new DateTime().getMillis() / 1000),
                            (int) (new DateTime().plusMonths(1).getMillis() / 1000),
                            homeGdg.getGplusId())
                    .enqueue(new Callback<ArrayList<Event>>() {
                        @Override
                        public void onSuccessResponse(ArrayList<Event> events) {
                            Timber.d("Got events");
                            if (events.size() > 0) {
                                Event firstEvent = events.get(0);
                                views.setTextViewText(R.id.title, firstEvent.getTitle());
                                views.setTextViewText(R.id.location, firstEvent.getLocation());
                                views.setTextViewText(R.id.startDate,
                                        firstEvent.getStart().toLocalDateTime()
                                                .toString(DateTimeFormat.patternForStyle("MS", getResources().getConfiguration().locale)));
                                showChild(views, 1);

                                Intent i = new Intent(UpdateService.this, EventActivity.class);
                                i.putExtra(Const.EXTRA_EVENT_ID, firstEvent.getId());
                                views.setOnClickPendingIntent(R.id.container, PendingIntent.getActivity(UpdateService.this, 0, i, 0));

                            } else {
                                showErrorChild(views, R.string.no_scheduled_events, UpdateService.this);
                            }
                            manager.updateAppWidget(thisWidget, views);
                        }

                        @Override
                        public void onFailure(Throwable t, int errorMessage) {

                            showErrorChild(views,
                                    errorMessage == R.string.offline_alert
                                            ? errorMessage : R.string.loading_data_failed,
                                    UpdateService.this);
                            manager.updateAppWidget(thisWidget, views);
                        }
                    });
        }

        private void showErrorChild(RemoteViews views, int errorStringResource, Context context) {
            views.setTextViewText(R.id.textView_no_events, getString(errorStringResource));
            showChild(views, 0);
            Intent i = new Intent(context, MainActivity.class);
            views.setOnClickPendingIntent(R.id.container, PendingIntent.getActivity(context, 0, i, 0));
        }

        private void showChild(RemoteViews views, int i) {
            views.setDisplayedChild(R.id.viewFlipper, i);
        }
    }

    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        context.startService(new Intent(context, UpdateService.class));
    }
}
