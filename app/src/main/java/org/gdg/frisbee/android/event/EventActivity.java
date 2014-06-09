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

package org.gdg.frisbee.android.event;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.viewpagerindicator.TitlePageIndicator;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.api.GroupDirectory;

import butterknife.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 23:03
 */
public class EventActivity extends GdgActivity {
    private static String LOG_TAG = "GDG-EventActivity";

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @InjectView(R.id.titles)
    TitlePageIndicator mIndicator;

    private EventPagerAdapter mViewPagerAdapter;
    private String mEventId;
    private GroupDirectory mClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(R.string.event);


        mIndicator.setOnPageChangeListener(this);

        mViewPagerAdapter = new EventPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mIndicator.setViewPager(mViewPager);

        mEventId = getIntent().getStringExtra(Const.EXTRA_EVENT_ID);
        String section = getIntent().getStringExtra(Const.EXTRA_SECTION);
        if (EventPagerAdapter.SECTION_OVERVIEW.equals(section)){
            mIndicator.setCurrentItem(0);
        }
    }

    protected String getTrackedViewName() {
        return "Event/"+getResources().getStringArray(R.array.about_tabs)[getCurrentPage()];
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class EventPagerAdapter extends FragmentStatePagerAdapter {
        public static final String SECTION_OVERVIEW = "overview";
        private Context mContext;

        public EventPagerAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            int count = mContext.getResources().getStringArray(R.array.event_tabs).length;
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return EventOverviewFragment.createFor(mEventId);
                case 1:
                    return new AgendaFragment();
                case 2:
                    return new MoreFragment();
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getResources().getStringArray(R.array.event_tabs)[position];
        }
    }
}
