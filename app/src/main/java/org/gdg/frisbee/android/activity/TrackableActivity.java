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

package org.gdg.frisbee.android.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import org.gdg.frisbee.android.app.App;

/**
 * Activity which provides handy mechanism for google analytics.
 *
 * Every extending Activity needs to implement @ref getTrackedViewName() method
 * which should return view name for tracking, without leading '/'.
 * Each extending Activity is tracked on {@link #onCreate(Bundle)} and {@link #onResume()}
 * methods. Additionally it can be tracked when {@link #onPageSelected(int)} event is
 * fired, but one must first register for such event.
 *
 * @author Bartosz Przybylski <bart.p.pl@gmail.com>
 */
public abstract class TrackableActivity extends ActionBarActivity implements ViewPager.OnPageChangeListener {

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
    public void onPageScrolled(int i, float v, int i2) {}

    @Override
    public void onPageSelected(int i) {
        mCurrentPage = i;
        trackView(getTrackedViewName());
    }

    @Override
    public void onPageScrollStateChanged(int i) {}

    protected void trackView() {
        trackView(getTrackedViewName());
    }

    protected void trackView(String viewName) {
        if(viewName != null) {
            App.getInstance().getTracker().sendView("/" + viewName);
        }
    }
}
