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

package org.gdg.frisbee.android.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.Utils;

import io.doorbell.android.Doorbell;

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

    protected void trackView() {
        trackView(getTrackedViewName());
    }

    protected void trackView(String viewName) {
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
        Doorbell doorbellDialog = new Doorbell(this, BuildConfig.DOORBELL_ID, BuildConfig.DOORBELL_APP_KEY); // Create the Doorbell object

        doorbellDialog.addProperty("loggedIn", PrefUtils.isSignedIn(this)); // Optionally add some properties
        doorbellDialog.addProperty("appStarts", PrefUtils.getAppStarts(this));
        doorbellDialog.addProperty("appVersionCode", PrefUtils.getVersionCode(this));


        // Callback for when the dialog is shown
        doorbellDialog.setOnShowCallback(new io.doorbell.android.callbacks.OnShowCallback() {
            @Override
            public void handle() {
                trackView("Feedback/" + getTrackedViewName());
            }
        });

        AlertDialog dialog = doorbellDialog.show();
        fixAlertDialogMargins(dialog);
    }

    private void fixAlertDialogMargins(AlertDialog dialog) {
        View customView = dialog.findViewById(android.R.id.custom);
        if (customView != null && customView instanceof FrameLayout) {

            if (customView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams =
                        (FrameLayout.LayoutParams) customView.getLayoutParams();
                int margin = Utils.dpToPx(getResources(), 16);
                layoutParams.setMargins(margin, margin, margin, margin);
            }

            FrameLayout frameLayout = (FrameLayout) customView;
            for (int i = 0; i < frameLayout.getChildCount(); i++) {
                View child = frameLayout.getChildAt(i);
                if (child instanceof LinearLayout) {
                    final LinearLayout linearLayout = (LinearLayout) child;
                    for (int j = 0; j < linearLayout.getChildCount(); j++) {
                        LinearLayout.LayoutParams childParams =
                                (LinearLayout.LayoutParams) linearLayout.getChildAt(j).getLayoutParams();
                        childParams.setMargins(0, 0, 0, Utils.dpToPx(getResources(), 8));
                    }
                }
            }
        }
    }
}
