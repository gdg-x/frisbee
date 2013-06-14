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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.model.Chapter;
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
public class FirstStartActivity extends RoboSherlockFragmentActivity {

    private static String LOG_TAG = "GDG-FirstStartActivity";

    @InjectView(R.id.pager)
    private NonSwipeableViewPager mViewPager;

    private ChapterAdapter mSpinnerAdapter;
    private FirstStartPageAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_first_start);

        mViewPagerAdapter = new FirstStartPageAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
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
                    return FirstStartStep1Fragment.newInstance();
                case 1:
                    return FirstStartStep2Fragment.newInstance();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
