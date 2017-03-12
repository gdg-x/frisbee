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

package org.gdg.frisbee.android.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.about.AboutActivity;
import org.gdg.frisbee.android.activity.SettingsActivity;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.gdg.frisbee.android.eventseries.TaggedEventSeriesActivity;
import org.gdg.frisbee.android.gde.GdeActivity;
import org.gdg.frisbee.android.onboarding.AppInviteLinkGenerator;
import org.gdg.frisbee.android.pulse.PulseActivity;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.widget.FeedbackFragment;

import java.util.List;

import butterknife.BindView;

public abstract class GdgNavDrawerActivity extends GdgActivity {

    private static final int DRAWER_HOME = 0;
    private static final int DRAWER_PULSE = 2;
    private static final int DRAWER_GDE = 5;
    private static final int DRAWER_SETTINGS = 100;
    private static final int DRAWER_INVITE = 101;
    private static final int DRAWER_HELP = 102;
    private static final int DRAWER_FEEDBACK = 103;
    private static final int DRAWER_ABOUT = 104;

    // Drawer Special Event Items
    public static final int DRAWER_DEVFEST = 30;
    public static final int DRAWER_WTM = 31;
    public static final int DRAWER_STUDY_JAM = 32;
    public static final int DRAWER_IO_EXTENDED = 33;
    public static final int DRAWER_GCP_NEXT = 34;

    private static final String EXTRA_SELECTED_DRAWER_ITEM_ID = "SELECTED_DRAWER_ITEM_ID";

    private static final int GROUP_ID = 1;
    private static final int SETTINGS_GROUP_ID = 2;

    @BindView(R.id.drawer)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    private DrawerHeaderDisplayer headerDisplayer;

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        if (toolbar != null) {
            DrawerArrowDrawable drawerIcon = new DrawerArrowDrawable(getSupportActionBar().getThemedContext());
            toolbar.setNavigationIcon(drawerIcon);
        }
        setupDrawerContent(mNavigationView);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        populateMainGroup(menu);
        populateSettingsGroup(menu);

        int selectedDrawerItemId = getIntent().getIntExtra(EXTRA_SELECTED_DRAWER_ITEM_ID, DRAWER_HOME);
        navigationView.setCheckedItem(selectedDrawerItemId);
        navigationView.setNavigationItemSelectedListener(createDrawerItemClickListener());
        headerDisplayer = new DrawerHeaderDisplayer(navigationView.getHeaderView(0), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSignInClick();
            }
        });
    }

    private void populateMainGroup(Menu menu) {
        menu.add(GROUP_ID, DRAWER_HOME, Menu.NONE, R.string.home_gdg).setIcon(R.drawable.ic_home_b4b4b4_24dp);
        menu.add(GROUP_ID, DRAWER_GDE, Menu.NONE, R.string.gde).setIcon(R.drawable.ic_drawer_gde);
        menu.add(GROUP_ID, DRAWER_PULSE, Menu.NONE, R.string.pulse).setIcon(R.drawable.ic_drawer_pulse);

        //adding special events in navigation drawer
        final List<TaggedEventSeries> currentEventSeries =
            App.from(this).currentTaggedEventSeries();
        for (TaggedEventSeries taggedEventSeries : currentEventSeries) {
            menu.add(GROUP_ID,
                taggedEventSeries.getDrawerId(),
                Menu.NONE,
                taggedEventSeries.getTitleResId()
            ).setIcon(taggedEventSeries.getDrawerIconResId());
        }
        menu.setGroupCheckable(GROUP_ID, true, true);
    }

    private static void populateSettingsGroup(Menu menu) {
        menu.add(SETTINGS_GROUP_ID, DRAWER_SETTINGS, Menu.NONE, R.string.settings)
            .setIcon(R.drawable.ic_drawer_settings);
        menu.add(SETTINGS_GROUP_ID, DRAWER_INVITE, Menu.NONE, R.string.invite_friends)
            .setIcon(R.drawable.ic_drawer_invite);
        menu.add(SETTINGS_GROUP_ID, DRAWER_HELP, Menu.NONE, R.string.help)
            .setIcon(R.drawable.ic_drawer_help);
        menu.add(SETTINGS_GROUP_ID, DRAWER_FEEDBACK, Menu.NONE, R.string.feedback)
            .setIcon(R.drawable.ic_drawer_feedback);
        menu.add(SETTINGS_GROUP_ID, DRAWER_ABOUT, Menu.NONE, R.string.about)
            .setIcon(R.drawable.ic_drawer_about);
    }

    private NavigationView.OnNavigationItemSelectedListener createDrawerItemClickListener() {
        return new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                onDrawerItemClick(menuItem.getItemId());
                mDrawerLayout.closeDrawers();
                return false;
            }
        };
    }

    private void onSignInClick() {
        if (PrefUtils.isSignedIn(this)) {
            return;
        }
        closeNavDrawer();
        requestSignIn();
    }

    @Override
    protected void onSuccessfulSignIn(GoogleSignInAccount signInAccount) {
        super.onSuccessfulSignIn(signInAccount);
        headerDisplayer.updateUserDetails(PlusUtils.getCurrentAccount(this));
        String welcome = getString(R.string.welcome_sign_in, signInAccount.getDisplayName());
        Toast.makeText(this, welcome, Toast.LENGTH_SHORT).show();
    }

    void onDrawerItemClick(int itemId) {
        Bundle data = new Bundle();
        data.putInt(EXTRA_SELECTED_DRAWER_ITEM_ID, itemId);

        switch (itemId) {
            case DRAWER_HOME:
                navigateTo(MainActivity.class, data);
                break;
            case DRAWER_GDE:
                navigateTo(GdeActivity.class, data);
                break;
            case DRAWER_DEVFEST:
            case DRAWER_WTM:
            case DRAWER_STUDY_JAM:
            case DRAWER_IO_EXTENDED:
            case DRAWER_GCP_NEXT:
                onDrawerSpecialItemClick(itemId, data);
                break;
            case DRAWER_PULSE:
                navigateTo(PulseActivity.class, data);
                break;
            case DRAWER_SETTINGS:
                navigateTo(SettingsActivity.class, data);
                break;
            case DRAWER_INVITE:
                AppInviteLinkGenerator.shareAppInviteLink(this);
                break;
            case DRAWER_HELP:
                startActivity(Utils.createExternalIntent(this, Uri.parse(Const.URL_HELP)));
                break;
            case DRAWER_FEEDBACK:
                displayFeedbackDialog();
                break;
            case DRAWER_ABOUT:
                navigateTo(AboutActivity.class, data);
                break;
        }
    }

    private void onDrawerSpecialItemClick(int itemId, Bundle data) {
        if (this instanceof TaggedEventSeriesActivity) {
            TaggedEventSeriesActivity activity = (TaggedEventSeriesActivity) this;
            TaggedEventSeries taggedEventSeries = activity.getTaggedEventSeries();

            if (taggedEventSeries.getDrawerId() == itemId) {
                return;
            }
        }
        final List<TaggedEventSeries> currentEventSeries = App.from(this).currentTaggedEventSeries();
        for (TaggedEventSeries taggedEventSeries : currentEventSeries) {
            if (taggedEventSeries.getDrawerId() == itemId) {

                data.putString(Const.EXTRA_TAGGED_EVENT_CACHEKEY, taggedEventSeries.getTag());
                data.putParcelable(Const.EXTRA_TAGGED_EVENT, taggedEventSeries);
                navigateTo(TaggedEventSeriesActivity.class, data);

                break;
            }
        }
    }

    private void displayFeedbackDialog() {
        trackView("Feedback/" + getTrackedViewName());
        new FeedbackFragment().show(getSupportFragmentManager(), "FeedbackFragment");
    }

    private void navigateTo(Class<? extends Activity> activityClass, @Nullable Bundle additional) {
        if (this.getClass().equals(activityClass)
            && !(this instanceof TaggedEventSeriesActivity)) {
            return;
        }

        Intent intent = new Intent(GdgNavDrawerActivity.this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (additional != null) {
            intent.putExtras(additional);
        }
        startActivity(intent);
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            toggleNavDrawer();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleNavDrawer() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        headerDisplayer.maybeUpdateChapterImage(PrefUtils.getHomeChapterId(this));
        headerDisplayer.updateUserDetails(PlusUtils.getCurrentAccount(this));
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isNavDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void closeNavDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

}
