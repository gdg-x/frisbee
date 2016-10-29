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

package org.gdg.frisbee.android.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.app.App;

import timber.log.Timber;

/**
 * Activity which provides handy mechanism for google analytics.
 * <p/>
 * Every extending Activity needs to implement @ref getTrackedViewName() method
 * which should return view name for tracking, without leading '/'.
 * Each extending Activity is tracked on {@link #onCreate(Bundle)} and {@link #onResume()}
 * methods. Additionally it can be tracked when {@link #onPageSelected(int)} event is
 * fired, but one must first register for such event.
 *
 */
public abstract class TrackableActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private int mCurrentPage = 0;

    protected abstract String getTrackedViewName();

    protected int getCurrentPage() {
        return mCurrentPage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackView(getTrackedViewName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackView(getTrackedViewName());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
        trackView(getTrackedViewName());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    protected final void trackView() {
        trackView(getTrackedViewName());
    }

    public final void trackView(String viewName) {
        if (viewName == null) {
            return;
        }

        if (BuildConfig.DEBUG) {
            Timber.tag("Analytics").d("Screen: " + viewName);
        } else {
            Tracker t = App.getInstance().getTracker();
            // Set screen name.
            // Where path is a String representing the screen name.
            t.setScreenName("/" + viewName);

            // Send a screen view.
            t.send(new HitBuilders.AppViewBuilder().build());

            Answers.getInstance().logContentView(new ContentViewEvent().putContentName(viewName));
        }
    }

    public final void sendAnalyticsEvent(@NonNull String category,
                                   @NonNull String action,
                                   @NonNull String label,
                                   long value) {
        if (BuildConfig.DEBUG) {
            Timber.tag("Analytics").d("Event recorded:"
                    + "\n\tCategory: " + category
                    + "\n\tAction: " + action
                    + "\n\tLabel: " + label
                    + "\n\tValue: " + value);
        } else {
            App.getInstance().getTracker()
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .setLabel(label)
                            .setValue(value)
                            .build());

            Answers.getInstance()
                    .logCustom(new CustomEvent(category)
                            .putCustomAttribute(action, label)
                            .putCustomAttribute("value", value));
        }
    }

    public final void sendAnalyticsEvent(@NonNull String category,
                                   @NonNull String action,
                                   @NonNull String label) {
        sendAnalyticsEvent(category, action, label, 0);
    }
}
