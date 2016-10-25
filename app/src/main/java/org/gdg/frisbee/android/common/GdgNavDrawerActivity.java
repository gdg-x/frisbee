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

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.about.AboutActivity;
import org.gdg.frisbee.android.activity.SettingsActivity;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.plus.Person;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.gdg.frisbee.android.eventseries.TaggedEventSeriesActivity;
import org.gdg.frisbee.android.gde.GdeActivity;
import org.gdg.frisbee.android.onboarding.AppInviteLinkGenerator;
import org.gdg.frisbee.android.pulse.PulseActivity;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.CircleTransform;
import org.joda.time.DateTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    ImageView mDrawerImage;
    ImageView mDrawerUserPicture;
    TextView mDrawerUserName;
    private ActionBarDrawerToggle mDrawerToggle;

    private String mStoredHomeChapterId;

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);

        initNavigationDrawer();
    }

    private void initNavigationDrawer() {

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
                if (PrefUtils.shouldOpenDrawerOnStart(GdgNavDrawerActivity.this)) {
                    PrefUtils.setShouldNotOpenDrawerOnStart(GdgNavDrawerActivity.this);
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

        if (mNavigationView != null) {
            setupDrawerContent(mNavigationView);
        }

    }

    private void setupDrawerContent(NavigationView navigationView) {

        Menu menu = navigationView.getMenu();
        menu.add(GROUP_ID, DRAWER_HOME, Menu.NONE, R.string.home_gdg).setIcon(R.drawable.ic_drawer_home_gdg);
        menu.add(GROUP_ID, DRAWER_GDE, Menu.NONE, R.string.gde).setIcon(R.drawable.ic_drawer_gde);
        menu.add(GROUP_ID, DRAWER_PULSE, Menu.NONE, R.string.pulse).setIcon(R.drawable.ic_drawer_pulse);

        //adding special events in navigation drawer
        final List<TaggedEventSeries> currentEventSeries =
            App.getInstance().currentTaggedEventSeries();
        for (TaggedEventSeries taggedEventSeries : currentEventSeries) {
            menu.add(GROUP_ID,
                taggedEventSeries.getDrawerId(),
                Menu.NONE,
                taggedEventSeries.getTitleResId())
                .setIcon(taggedEventSeries.getDrawerIconResId());
        }

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

        menu.setGroupCheckable(GROUP_ID, true, true);

        final int selectedDrawerItemId = getIntent().getIntExtra(EXTRA_SELECTED_DRAWER_ITEM_ID, DRAWER_HOME);
        final MenuItem selectedItem = menu.findItem(selectedDrawerItemId);
        if (selectedItem != null) {
            selectedItem.setChecked(true);
        }

        navigationView.setNavigationItemSelectedListener(

            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    onDrawerItemClick(menuItem.getItemId());
                    mDrawerLayout.closeDrawers();
                    return true;
                }
            });
        View headerView = navigationView.getHeaderView(0);
        mDrawerImage = ButterKnife.findById(headerView, R.id.navdrawer_image);
        mDrawerUserPicture = ButterKnife.findById(headerView, R.id.navdrawer_user_picture);
        mDrawerUserName = ButterKnife.findById(headerView, R.id.navdrawer_user_name);

        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSignInClick();
            }
        });
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
        String welcome = getString(R.string.welcome_sign_in, signInAccount.getDisplayName());
        Toast.makeText(this, welcome, Toast.LENGTH_SHORT).show();
    }

    void onDrawerItemClick(int itemId) {

        if (PrefUtils.shouldOpenDrawerOnStart(GdgNavDrawerActivity.this)) {
            PrefUtils.setShouldNotOpenDrawerOnStart(GdgNavDrawerActivity.this);
        }
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
                showFeedbackDialog();
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

        final List<TaggedEventSeries> currentEventSeries =
            App.getInstance().currentTaggedEventSeries();
        for (TaggedEventSeries taggedEventSeries : currentEventSeries) {
            if (taggedEventSeries.getDrawerId() == itemId) {

                data.putString(Const.EXTRA_TAGGED_EVENT_CACHEKEY, taggedEventSeries.getTag());
                data.putParcelable(Const.EXTRA_TAGGED_EVENT, taggedEventSeries);
                navigateTo(TaggedEventSeriesActivity.class, data);

                break;
            }
        }
    }

    private void navigateTo(Class<? extends GdgActivity> activityClass, Bundle additional) {
        if (this.getClass().equals(activityClass)
            && !(this instanceof TaggedEventSeriesActivity)) {
            return;
        }

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

        if (PrefUtils.shouldOpenDrawerOnStart(this)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        maybeUpdateChapterImage();
        updateUserDetails();
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
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void updateUserDetails() {
        final GoogleSignInAccount account = PlusUtils.getCurrentAccount(this);
        if (account == null) {
            mDrawerUserPicture.setImageDrawable(null);
            mDrawerUserName.setText(R.string.login_register);
            return;
        }
        if (account.getPhotoUrl() != null) {
            App.getInstance().getPicasso().load(account.getPhotoUrl())
                .transform(new CircleTransform())
                .into(mDrawerUserPicture);
        }
        String displayName = account.getDisplayName();
        if (!TextUtils.isEmpty(displayName)) {
            mDrawerUserName.setText(displayName);
        }
    }

    private void maybeUpdateChapterImage() {
        final String homeChapterId = PrefUtils.getHomeChapterId(this);
        if (isHomeChapterOutdated(homeChapterId)) {
            App.getInstance().getModelCache().getAsync(ModelCache.KEY_PERSON + homeChapterId,
                true, new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object person) {
                        updateChapterImage((Person) person, homeChapterId);
                    }

                    @Override
                    public void onNotFound(final String key) {
                        App.getInstance().getPlusApi().getPerson(homeChapterId).enqueue(new Callback<Person>() {
                            @Override
                            public void success(Person person) {
                                if (person != null) {
                                    App.getInstance().getModelCache().putAsync(key, person,
                                        DateTime.now().plusDays(1), null);
                                    updateChapterImage(person, homeChapterId);
                                }
                            }
                        });
                    }
                });
        }
    }

    private void updateChapterImage(Person person, String homeChapterId) {
        mStoredHomeChapterId = homeChapterId;
        if (person.getCover() != null) {
            App.getInstance().getPicasso()
                .load(person.getCover().getCoverPhoto().getUrl())
                .into(mDrawerImage);
        }
    }

    protected boolean isHomeChapterOutdated(final String currentHomeChapterId) {
        return currentHomeChapterId != null
            && (mStoredHomeChapterId == null || !mStoredHomeChapterId.equals(currentHomeChapterId));
    }
}
