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
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.widget.NonSwipeableViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class FirstStartActivity extends AppCompatActivity implements
        FirstStartStep1Fragment.Step1Listener, 
        FirstStartStep2Fragment.Step2Listener, 
        FirstStartStep3Fragment.Step3Listener {

    public static final String ACTION_FIRST_START = "finish_first_start";

    @Bind(R.id.pager)
    NonSwipeableViewPager mViewPager;

    @Bind(R.id.contentLayout)
    FrameLayout mContentLayout;

    private FirstStartPageAdapter mViewPagerAdapter;

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_start);

        App.getInstance().updateLastLocation();

        ButterKnife.bind(this);

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
    public void onConfirmedChapter(Chapter chapter) {
        PrefUtils.setHomeChapter(this, chapter);

        if (mGoogleApiClient == null) {
            mViewPager.setCurrentItem(1, true);
        } else {
            mViewPager.setCurrentItem(2, true);
        }
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
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        } else {
            super.finish();
        }
    }

    @Override
    public void onSignedIn(GoogleApiClient client, String accountName) {

        mGoogleApiClient = client;

        Timber.d("Signed in.");
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(true);

        PrefUtils.setSignedIn(this);

        if (mViewPager.getCurrentItem() >= 1) {
            mViewPager.setCurrentItem(2, true);
        }
    }

    @Override
    public void onSkippedSignIn() {
        PrefUtils.setLoggedOut(this);
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(false);

        mViewPager.setCurrentItem(2, true);
    }

    @Override
    public void finish() {

        requestBackup();

        Intent resultData = new Intent(this, MainActivity.class);
        resultData.setAction(ACTION_FIRST_START);
        startActivity(resultData);

        super.finish();
    }

    private void requestBackup() {
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }

    @Override
    public void onComplete(final boolean enableAnalytics, final boolean enableGcm) {
        PrefUtils.setInitialSettings(FirstStartActivity.this, enableGcm, enableAnalytics, null, null);
        finish();
    }

    public class FirstStartPageAdapter extends FragmentStatePagerAdapter {
        private Fragment[] mFragments;

        public FirstStartPageAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[] {
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

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
