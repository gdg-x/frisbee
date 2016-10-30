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

package org.gdg.frisbee.android.about;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.onboarding.AppInviteLinkGenerator;

import butterknife.BindView;

public class AboutActivity extends GdgActivity {

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPager.setAdapter(new AboutPagerAdapter(getSupportFragmentManager(), getResources()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    protected String getTrackedViewName() {
        return "About/" + getResources().getStringArray(R.array.about_tabs)[getCurrentPage()];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        } else if (R.id.action_app_invite == item.getItemId()) {
            AppInviteLinkGenerator.shareAppInviteLink(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
