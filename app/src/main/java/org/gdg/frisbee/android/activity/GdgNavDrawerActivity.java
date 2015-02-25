/*
 * Copyright 2013-2015 The GDG Frisbee Project
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

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.games.Games;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.DrawerAdapter;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.OrganizerChecker;
import org.gdg.frisbee.android.special.SpecialEventActivity;
import org.gdg.frisbee.android.special.TaggedEvent;
import org.gdg.frisbee.android.utils.PrefUtils;

import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public abstract class GdgNavDrawerActivity extends GdgActivity {

    protected DrawerAdapter mDrawerAdapter;
    protected ActionBarDrawerToggle mDrawerToggle;
    @InjectView(R.id.drawer)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.left_drawer)
    ListView mDrawerContent;

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);

        initNavigationDrawer();
    }

    private void initNavigationDrawer() {
        mDrawerAdapter = new DrawerAdapter(this);
        mDrawerContent.setAdapter(mDrawerAdapter);
        mDrawerContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DrawerAdapter.DrawerItem item = (DrawerAdapter.DrawerItem) mDrawerAdapter.getItem(i);

                switch (item.getId()) {
                    case Const.DRAWER_ACHIEVEMENTS:
                        if (PrefUtils.isSignedIn(GdgNavDrawerActivity.this) && getGoogleApiClient().isConnected()) {
                            startActivityForResult(Games.Achievements.getAchievementsIntent(getGoogleApiClient()), 0);
                        } else {
                            Crouton.makeText(GdgNavDrawerActivity.this, getString(R.string.achievements_need_signin), Style.INFO).show();
                        }
                        break;
                    case Const.DRAWER_HOME:
                        navigateTo(MainActivity.class, null);
                        break;
                    case Const.DRAWER_GDE:
                        navigateTo(GdeActivity.class, null);
                        break;
                    case Const.DRAWER_SPECIAL:
                        final TaggedEvent taggedEvent = TaggedEvent.getCurrent();
                        Bundle special = new Bundle();
                        special.putString(Const.EXTRA_TAGGED_EVENT_CACHEKEY, taggedEvent.getTag());
                        special.putParcelable(Const.EXTRA_TAGGED_EVENT, taggedEvent);
                        navigateTo(SpecialEventActivity.class, special);
                        break;
                    case Const.DRAWER_PULSE:
                        navigateTo(PulseActivity.class, null);
                        break;
                    case Const.DRAWER_ARROW:
                        if (PrefUtils.isSignedIn(GdgNavDrawerActivity.this) && getGoogleApiClient().isConnected()) {
                            navigateTo(ArrowActivity.class, null);
                        } else {
                            Crouton.makeText(GdgNavDrawerActivity.this, getString(R.string.arrow_need_games), Style.INFO).show();
                        }
                        break;
                    case Const.DRAWER_SETTINGS:
                        navigateTo(SettingsActivity.class, null);
                        break;
                    case Const.DRAWER_ABOUT:
                        navigateTo(AboutActivity.class, null);
                        break;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                if (PrefUtils.isFirstStartDone(GdgNavDrawerActivity.this)) {
                    PrefUtils.setFirstStartDone(GdgNavDrawerActivity.this);
                }
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    private void navigateTo(Class<? extends GdgActivity> activityClass, Bundle additional) {
        Intent i = new Intent(GdgNavDrawerActivity.this, activityClass);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if (additional != null) {
            i.putExtras(additional);
        }

        startActivity(i);
        mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!PrefUtils.isFirstStartDone(this)) {
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    public boolean isOrganizer() {
        return App.getInstance().isOrganizer();
    }

    protected void checkOrganizer(final OrganizerChecker.OrganizerResponseHandler responseHandler) {
        App.getInstance().checkOrganizer(getGoogleApiClient(), responseHandler);
    }
}
