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
import android.view.MenuItem;

import com.viewpagerindicator.TitlePageIndicator;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.fragment.*;

import butterknife.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 23:03
 */
public class AboutActivity extends GdgActivity {
    private static String LOG_TAG = "GDG-AboutActivity";

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @InjectView(R.id.titles)
    TitlePageIndicator mIndicator;

    private AboutPagerAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(R.string.about);


        mIndicator.setOnPageChangeListener(this);

        mViewPagerAdapter = new AboutPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mIndicator.setViewPager(mViewPager);
    }

    protected String getTrackedViewName() {
        return "About/"+getResources().getStringArray(R.array.about_tabs)[getCurrentPage()];
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class AboutPagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;

        public AboutPagerAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mContext.getResources().getStringArray(R.array.about_tabs).length;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return new AboutFragment();
                case 1:
                    return new ContributorsFragment();
                case 2:
                    return new ChangelogFragment();
                case 3:
                    return new GetInvolvedFragment();
                case 4:
                    return new ExtLibrariesFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getResources().getStringArray(R.array.about_tabs)[position];
        }
    }
}
