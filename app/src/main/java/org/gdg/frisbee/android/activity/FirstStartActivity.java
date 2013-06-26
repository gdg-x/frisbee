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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.fragment.*;
import org.gdg.frisbee.android.view.NonSwipeableViewPager;
import roboguice.inject.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 29.04.13
 * Time: 14:48
 */
public class FirstStartActivity extends RoboSherlockFragmentActivity implements FirstStartStep1Fragment.Step1Listener, FirstStartStep2Fragment.Step2Listener {

    private static String LOG_TAG = "GDG-FirstStartActivity";

    @InjectView(R.id.pager)
    private NonSwipeableViewPager mViewPager;

    private SharedPreferences mPreferences;
    private Chapter mSelectedChapter;
    private FirstStartPageAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_first_start);

        mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);

        mViewPagerAdapter = new FirstStartPageAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onPageSelected(int i) {
                App.getInstance().getTracker().sendView("/FirstStart/Step"+(1+i));
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    @Override
    public void onConfirmedChapter(Chapter chapter) {
        mSelectedChapter = chapter;
        mPreferences.edit()
                .putString(Const.SETTINGS_HOME_GDG, chapter.getChapterId())
                .commit();
        mViewPager.setCurrentItem(1, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FirstStartStep2Fragment.REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
            FirstStartStep2Fragment fragment = (FirstStartStep2Fragment) mViewPagerAdapter.getItem(mViewPager.getCurrentItem());
            PlusClient plusClient = new PlusClient.Builder(this, fragment, fragment)
                    .setScopes("https://www.googleapis.com/auth/youtube", Scopes.PLUS_LOGIN, Scopes.PLUS_PROFILE)
                    .build();
            fragment.setPlusClient(plusClient);
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        App.getInstance().getTracker().sendView("/FirstStart/Step"+(1+mViewPager.getCurrentItem()));
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() > 0)
            mViewPager.setCurrentItem(0, true);
        else
            super.onBackPressed();
    }

    @Override
    public void onSignedIn(String accountName) {
        mPreferences.edit()
                .putBoolean(Const.SETTINGS_FIRST_START, false)
                .putBoolean(Const.SETTINGS_SIGNED_IN, true)
                .commit();

        finish();
    }

    @Override
    public void onSkippedSignIn() {
        mPreferences.edit()
                .putBoolean(Const.SETTINGS_FIRST_START, false)
                .putBoolean(Const.SETTINGS_SIGNED_IN, false)
                .commit();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {
        Intent resultData = new Intent();
        resultData.putExtra("selected_chapter", mSelectedChapter);
        setResult(RESULT_OK, resultData);
        super.finish();
    }

    public class FirstStartPageAdapter extends FragmentStatePagerAdapter {
        private Context mContext;

        public FirstStartPageAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return FirstStartStep1Fragment.newInstance(FirstStartActivity.this);
                case 1:
                    return FirstStartStep2Fragment.newInstance(FirstStartActivity.this);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
