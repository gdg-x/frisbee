/*
 * Copyright 2013 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import timber.log.Timber;

public class UpcomingEventWidgetProvider extends AppWidgetProvider {

    public static final String LOG_TAG = "GDG-UpcomingEventWidgetProvider";

    public static class UpdateService extends Service {
        private SharedPreferences mPreferences;
        private GroupDirectory mDirectory;
        private ArrayList<Chapter> mChapters;

        @Override
        public void onStart(Intent intent, int startId) {
            Timber.d("onStart()");

            mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);
            mDirectory = new GroupDirectory();

            ComponentName thisWidget = new ComponentName(this, UpcomingEventWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);

            // Build the widget update for today
            buildUpdate(this, manager, thisWidget);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private Chapter findChapter(String chapterId) {
            if(chapterId != null) {
                for(Chapter chapter : mChapters) {
                    if(chapter.getGplusId().equals(chapterId)) {
                        return chapter;
                    }
                }
            }
            return null;
        }

        public void buildUpdate(final Context context, final AppWidgetManager manager, final ComponentName thisWidget) {
            // Pick out month names from resources
            final Resources res = context.getResources();

            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_upcoming_event);

            App.getInstance().getModelCache().getAsync("chapter_list",false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    mChapters = ((Directory) item).getGroups();
                    final Chapter homeGdg = findChapter(mPreferences.getString(Const.SETTINGS_HOME_GDG, null));

                    if(homeGdg == null) {
                        Timber.d("Got no Home GDG");
                        showChild(views, 0);
                    } else {
                        Timber.d("Fetching events");
                        String groupName = homeGdg.getName().replaceAll("GDG ","");
                        views.setTextViewText(R.id.groupName, groupName);
                        views.setTextViewText(R.id.groupName2, groupName);
                        mDirectory.getChapterEventList(new DateTime(), new DateTime().plusMonths(1), homeGdg.getGplusId(), new Response.Listener<ArrayList<Event>>() {
                            @Override
                            public void onResponse(ArrayList<Event> events) {
                                Timber.d("Got events");
                                if (events.size() > 0) {
                                    views.setTextViewText(R.id.title, events.get(0).getTitle());
                                    views.setTextViewText(R.id.location, events.get(0).getLocation());
                                    views.setTextViewText(R.id.startDate, events.get(0).getStart().toLocalDateTime().toString(DateTimeFormat.patternForStyle("MS", res.getConfiguration().locale)));
                                    showChild(views, 1);

                                    if (events.get(0).getGPlusEventLink() != null) {

                                        String url = events.get(0).getGPlusEventLink();

                                        if (!url.startsWith("http")) {
                                            url = "https://" + url;
                                        }

                                        Intent i = new Intent(Intent.ACTION_VIEW);
                                        i.setData(Uri.parse(url));
                                        views.setOnClickPendingIntent(R.id.container, PendingIntent.getActivity(context, 0, i, 0));
                                    }
                                } else {
                                    showChild(views, 0);
                                }
                                manager.updateAppWidget(thisWidget, views);
                            }
                        }

                            ,new Response.ErrorListener()

                            {
                                @Override
                                public void onErrorResponse (VolleyError volleyError){
                                Timber.e("Error updating Widget", volleyError);
                                showChild(views, 0);
                                manager.updateAppWidget(thisWidget, views);
                            }
                            }

                            ).

                            execute();
                        }
                    }

                @Override
                public void onNotFound(String key) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });

        }

        private void showChild(RemoteViews views, int i) {
            if (i == 1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    views.setDisplayedChild(R.id.viewFlipper, 1);
                } else {
                    views.setViewVisibility(R.id.noEventContainer, View.GONE);
                    views.setViewVisibility(R.id.eventContainer, View.VISIBLE);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    views.setDisplayedChild(R.id.viewFlipper, 0);
                } else {
                    views.setViewVisibility(R.id.noEventContainer, View.VISIBLE);
                    views.setViewVisibility(R.id.eventContainer, View.GONE);
                }
            }
        }
    }

    @Override
    public void onUpdate(Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        context.startService(new Intent(context, UpdateService.class));

        final int N = appWidgetIds.length;


        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            final int appWidgetId = appWidgetIds[i];


        }

        context.startService(new Intent(context, UpdateService.class));
    }
}
