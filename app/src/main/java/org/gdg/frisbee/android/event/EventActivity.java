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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AndroidAppUri;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.common.GdgActivity;

import butterknife.Bind;

public class EventActivity extends GdgActivity implements EventOverviewFragment.Callbacks {

    @Bind(R.id.pager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    private String mEventId;
    private EventFullDetails mEventFullDetails;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);
        getActionBarToolbar().setTitle(R.string.event);
        getActionBarToolbar().setNavigationIcon(R.drawable.ic_up);

        final EventPagerAdapter mViewPagerAdapter = new EventPagerAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);

        mEventId = getEventIdFrom(getIntent());
        String section = getIntent().getStringExtra(Const.EXTRA_SECTION);
        if (EventPagerAdapter.SECTION_OVERVIEW.equals(section)) {
            mViewPager.setCurrentItem(0);
        }
    }

    private String getEventIdFrom(Intent intent) {
        return intent.getStringExtra(Const.EXTRA_EVENT_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recordStartPageView();
    }

    @Override
    protected void onStop() {
        recordEndPageView();
        super.onStop();
    }

    protected String getTrackedViewName() {
        return "Event/" + getResources().getStringArray(R.array.event_tabs)[getCurrentPage()]
            + "/" + getEventIdFrom(getIntent());
    }

    private void recordStartPageView() {
        if (mEventFullDetails != null) {
            Action viewAction = createAppIndexAction(mEventFullDetails.getTitle(), mEventId);
            recordStartPageView(viewAction);
        }
    }

    private void recordEndPageView() {
        if (mEventFullDetails != null) {
            Action viewAction = createAppIndexAction(mEventFullDetails.getTitle(), mEventId);
            recordEndPageView(viewAction);
        }
    }

    @NonNull
    private Action createAppIndexAction(String title, String eventId) {
        final Uri hostUri = Uri.parse(Const.URL_GDGROUPS_ORG).buildUpon().appendPath(Const.PATH_GDGROUPS_ORG_EVENT)
            .appendPath(eventId).build();
        final Uri appUri = AndroidAppUri.newAndroidAppUri(BuildConfig.APPLICATION_ID, hostUri).toUri();
        return Action.newAction(Action.TYPE_VIEW, title, hostUri, appUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEventLoaded(EventFullDetails eventFullDetails) {
        mEventFullDetails = eventFullDetails;
        recordStartPageView();
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
