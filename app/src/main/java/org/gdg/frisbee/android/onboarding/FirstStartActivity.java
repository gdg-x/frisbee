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

package org.gdg.frisbee.android.onboarding;

import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.widget.NonSwipeableViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FirstStartActivity extends GdgActivity implements
    FirstStartStep1Fragment.Step1Listener,
    FirstStartStep2Fragment.Step2Listener,
    FirstStartStep3Fragment.Step3Listener {

    private static final String SIGN_IN_REQUESTED = "SIGN_IN_REQUESTED";
    public static final String ACTION_FIRST_START = "finish_first_start";

    @Bind(R.id.pager)
    NonSwipeableViewPager mViewPager;

    @Bind(R.id.contentLayout)
    FrameLayout mContentLayout;

    private FirstStartPageAdapter mViewPagerAdapter;
    private boolean signInRequested;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);
        ButterKnife.bind(this);

        App.getInstance().updateLastLocation();

        if (savedInstanceState != null) {
            signInRequested = savedInstanceState.getBoolean(SIGN_IN_REQUESTED);
        }

        mViewPagerAdapter = new FirstStartPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onPageSelected(int i) {
                Tracker t = App.getInstance().getTracker();
                // Set screen name.
                // Where path is a String representing the screen name.
                t.setScreenName("/FirstStart/Step" + (1 + i));

                // Send a screen view.
                t.send(new HitBuilders.AppViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SIGN_IN_REQUESTED, signInRequested);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewPagerAdapter.getItem(mViewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Tracker t = App.getInstance().getTracker();
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("/FirstStart/Step" + (1 + mViewPager.getCurrentItem()));

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        } else {
            super.finish();
        }
    }

    @Override
    public void onConfirmedChapter(Chapter chapter) {
        PrefUtils.setHomeChapter(this, chapter);

        if (PrefUtils.isSignedIn(this)) {
            moveToStep3(true);
        } else if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            == ConnectionResult.SERVICE_MISSING) {
            moveToStep3(false);
        } else {
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public void onSignedIn() {
        PrefUtils.setSignedIn(this);
        recreateGoogleApiClientIfNeeded();

        if (getGoogleApiClient().isConnected()) {
            moveToStep3(true);
        } else {
            getGoogleApiClient().connect();
            signInRequested = true;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        if (signInRequested && PrefUtils.isSignedIn(this)) {
            signInRequested = false;
            moveToStep3(true);
        }
    }

    @Override
    public void onSkippedSignIn() {
        PrefUtils.setLoggedOut(this);

        moveToStep3(false);
    }

    private void moveToStep3(final boolean isSignedIn) {
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(isSignedIn);

        mViewPager.setCurrentItem(2);
    }

    @Override
    public void onComplete(final boolean enableAnalytics, final boolean enableGcm) {
        PrefUtils.setInitialSettings(FirstStartActivity.this, enableAnalytics);
        requestBackup();

        startMainActivity();

        finish();
    }

    private void requestBackup() {
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }

    private void startMainActivity() {
        Intent resultData = new Intent(this, MainActivity.class);
        resultData.setAction(ACTION_FIRST_START);
        if (getIntent().getExtras() != null) {
            resultData.putExtras(getIntent().getExtras());
        }
        startActivity(resultData);
    }

    public class FirstStartPageAdapter extends FragmentStatePagerAdapter {
        private Fragment[] mFragments;

        public FirstStartPageAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[]{
                new FirstStartStep1Fragment(),
                new FirstStartStep2Fragment(),
                new FirstStartStep3Fragment()
            };
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }
    }
}
