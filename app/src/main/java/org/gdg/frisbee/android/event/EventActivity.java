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

package org.gdg.frisbee.android.event;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.view.SlidingTabLayout;

import butterknife.InjectView;

public class EventActivity extends GdgActivity {

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @InjectView(R.id.sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;

    private String mEventId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);
        getActionBarToolbar().setTitle(R.string.event);
        getActionBarToolbar().setNavigationIcon(R.drawable.ic_up);

        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setOnPageChangeListener(this);

        final EventPagerAdapter mViewPagerAdapter = new EventPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mSlidingTabLayout.setViewPager(mViewPager);

        mEventId = getIntent().getStringExtra(Const.EXTRA_EVENT_ID);
        String section = getIntent().getStringExtra(Const.EXTRA_SECTION);
        if (EventPagerAdapter.SECTION_OVERVIEW.equals(section)) {
            mViewPager.setCurrentItem(0);
        }
    }

    protected String getTrackedViewName() {
        return "Event/" + getResources().getStringArray(R.array.event_tabs)[getCurrentPage()] 
                + "/" + getIntent().getStringExtra(Const.EXTRA_EVENT_ID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
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
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return EventOverviewFragment.createfor(mEventId);
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
