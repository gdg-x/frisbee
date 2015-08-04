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
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.widget.FeedbackFragment;

/**
 * Activity which provides handy mechanism for google analytics.
 * <p/>
 * Every extending Activity needs to implement @ref getTrackedViewName() method
 * which should return view name for tracking, without leading '/'.
 * Each extending Activity is tracked on {@link #onCreate(Bundle)} and {@link #onResume()}
 * methods. Additionally it can be tracked when {@link #onPageSelected(int)} event is
 * fired, but one must first register for such event.
 *
 * @author Bartosz Przybylski <bart.p.pl@gmail.com>
 */
abstract class TrackableActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private int mCurrentPage = 0;

    @Nullable
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

    protected void trackView() {
        trackView(getTrackedViewName());
    }

    protected void trackView(@Nullable String viewName) {
        if (viewName != null) {
            Tracker t = App.getInstance().getTracker();
            // Set screen name.
            // Where path is a String representing the screen name.
            t.setScreenName("/" + viewName);

            // Send a screen view.
            t.send(new HitBuilders.AppViewBuilder().build());
        }
    }

    protected void showFeedbackDialog() {
        trackView("Feedback/" + getTrackedViewName());
        new FeedbackFragment().show(getSupportFragmentManager(), "FeedbackFragment");
    }
}
