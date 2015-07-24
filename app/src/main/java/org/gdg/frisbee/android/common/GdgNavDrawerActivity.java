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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.about.AboutActivity;
import org.gdg.frisbee.android.activity.SettingsActivity;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.arrow.ArrowActivity;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.gdg.frisbee.android.eventseries.TaggedEventSeriesActivity;
import org.gdg.frisbee.android.gde.GdeActivity;
import org.gdg.frisbee.android.pulse.PulseActivity;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.BitmapBorderTransformation;
import org.joda.time.DateTime;

import java.util.ArrayList;

import butterknife.InjectView;

public abstract class GdgNavDrawerActivity extends GdgActivity {

    protected ActionBarDrawerToggle mDrawerToggle;
    protected String mStoredHomeChapterId;
    @InjectView(R.id.drawer)
    DrawerLayout mDrawerLayout;
    @InjectView(R.id.navdrawer_image)
    ImageView mDrawerImage;
    @InjectView(R.id.navdrawer_user_picture)
    ImageView mDrawerUserPicture;
    @InjectView(R.id.nav_view)
    NavigationView mNavigationView;

    private Plus plusClient;
    private MenuItem drawerItemToNavigateAfterSignIn = null;
    private static final int GROUP_ID = 1;

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
        menu.add(GROUP_ID, Const.DRAWER_HOME, Menu.NONE, R.string.home_gdg).setIcon(R.drawable.ic_drawer_home_gdg);
        menu.add(GROUP_ID, Const.DRAWER_GDE, Menu.NONE, R.string.gde).setIcon(R.drawable.ic_drawer_gde);
        menu.add(GROUP_ID, Const.DRAWER_PULSE, Menu.NONE, R.string.pulse).setIcon(R.drawable.ic_drawer_pulse);

        //adding special events in navigation drawer
        final ArrayList<TaggedEventSeries> currentEventSeries =
                App.getInstance().currentTaggedEventSeries();
        for (TaggedEventSeries taggedEventSeries : currentEventSeries) {
            menu.add(GROUP_ID, Const.DRAWER_SPECIAL, Menu.NONE, taggedEventSeries.getTitleResId()).setIcon(taggedEventSeries.getDrawerIconResId());
        }

        menu.add(GROUP_ID, Const.DRAWER_ACHIEVEMENTS, Menu.NONE, R.string.achievements).setIcon(R.drawable.ic_drawer_achievements);
        menu.add(GROUP_ID, Const.DRAWER_ARROW, Menu.NONE, R.string.arrow).setIcon(R.drawable.ic_drawer_arrow);

        menu.addSubMenu("");
        menu.add(GROUP_ID, Const.DRAWER_SETTINGS, Menu.NONE, R.string.settings).setIcon(R.drawable.ic_drawer_settings);
        menu.add(GROUP_ID, Const.DRAWER_HELP, Menu.NONE, R.string.help).setIcon(R.drawable.ic_drawer_help);
        menu.add(GROUP_ID, Const.DRAWER_FEEDBACK, Menu.NONE, R.string.feedback).setIcon(R.drawable.ic_drawer_feedback);
        menu.add(GROUP_ID, Const.DRAWER_ABOUT, Menu.NONE, R.string.about).setIcon(R.drawable.ic_drawer_about);

        navigationView.setNavigationItemSelectedListener(

                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        onDrawerItemClick(menuItem);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void onDrawerItemClick(final MenuItem item) {

        if (PrefUtils.shouldOpenDrawerOnStart(GdgNavDrawerActivity.this)) {
            PrefUtils.setShouldNotOpenDrawerOnStart(GdgNavDrawerActivity.this);
        }

        switch (item.getItemId()) {
            case Const.DRAWER_ACHIEVEMENTS:
                if (PrefUtils.isSignedIn(this) && getGoogleApiClient().isConnected()) {
                    startActivityForResult(Games.Achievements.getAchievementsIntent(getGoogleApiClient()), 0);
                } else {
                    drawerItemToNavigateAfterSignIn = item;
                    showLoginErrorDialog(R.string.achievements_need_signin);
                }
                break;
            case Const.DRAWER_HOME:
                navigateTo(MainActivity.class, null);
                break;
            case Const.DRAWER_GDE:
                navigateTo(GdeActivity.class, null);
                break;
            case Const.DRAWER_SPECIAL:
                onDrawerSpecialItemClick(item);
                break;
            case Const.DRAWER_PULSE:
                navigateTo(PulseActivity.class, null);
                break;
            case Const.DRAWER_ARROW:
                if (PrefUtils.isSignedIn(this) && getGoogleApiClient().isConnected()) {
                    navigateTo(ArrowActivity.class, null);
                } else {
                    drawerItemToNavigateAfterSignIn = item;
                    showLoginErrorDialog(R.string.arrow_need_games);
                }
                break;
            case Const.DRAWER_SETTINGS:
                navigateTo(SettingsActivity.class, null);
                break;
            case Const.DRAWER_HELP:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Const.URL_HELP)));
                break;
            case Const.DRAWER_FEEDBACK:
                showFeedbackDialog();
                break;
            case Const.DRAWER_ABOUT:
                navigateTo(AboutActivity.class, null);
                break;
        }
    }

    private void showLoginErrorDialog(@StringRes int errorMessage) {

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_signing_needed)
                .setMessage(errorMessage)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PrefUtils.setSignedIn(GdgNavDrawerActivity.this);
                        if (!getGoogleApiClient().isConnected()) {
                            getGoogleApiClient().connect();
                        }
                    }
                })
                .show();
    }

    public void onDrawerSpecialItemClick(MenuItem item) {

        final ArrayList<TaggedEventSeries> currentEventSeries =
                App.getInstance().currentTaggedEventSeries();
        for (TaggedEventSeries taggedEventSeries : currentEventSeries) {
            if (getString(taggedEventSeries.getTitleResId()).equals(item.getTitle())) {

                Bundle special = new Bundle();
                special.putString(Const.EXTRA_TAGGED_EVENT_CACHEKEY, taggedEventSeries.getTag());
                special.putParcelable(Const.EXTRA_TAGGED_EVENT, taggedEventSeries);
                navigateTo(TaggedEventSeriesActivity.class, special);

                break;
            }
        }
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
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        plusClient = new Plus.Builder(mTransport, mJsonFactory, null)
//                .setGoogleClientRequestInitializer(
//                        new CommonGoogleJsonClientRequestInitializer(BuildConfig.ANDROID_SIMPLE_API_ACCESS_KEY))
//                .setApplicationName("GDG Frisbee")
//                .build();
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
    }

    @Override
    public void onConnected(final Bundle bundle) {
        super.onConnected(bundle);
        updateUserPicture();

        if (drawerItemToNavigateAfterSignIn != null) {
            onDrawerItemClick(drawerItemToNavigateAfterSignIn);
            drawerItemToNavigateAfterSignIn = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    protected void updateUserPicture() {
        com.google.android.gms.plus.model.people.Person user = com.google.android.gms.plus.Plus.PeopleApi.getCurrentPerson(getGoogleApiClient());
        com.google.android.gms.plus.model.people.Person.Image userPicture = user.getImage();
        if (userPicture != null && userPicture.hasUrl()) {
            App.getInstance().getPicasso().load(userPicture.getUrl())
                    .transform(new BitmapBorderTransformation(2,
                            getResources().getDimensionPixelSize(R.dimen.navdrawer_user_picture_size) / 2,
                            getResources().getColor(R.color.white)))
                    .into(mDrawerUserPicture);
        }
    }

    private void maybeUpdateChapterImage() {
        final String homeChapterId = getCurrentHomeChapterId();
        if (isHomeChapterOutdated(homeChapterId)) {
            new Builder<>(String.class, Person.class)
                    .addParameter(homeChapterId)
                    .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, Person>() {
                        @Override
                        public Person doInBackground(String... params) {
                            return getPersonSync(getGoogleApiClient(), params[0]);
                        }
                    })
                    .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<String, Person>() {
                        @Override
                        public void onPostExecute(String[] params, Person person) {
                            if (person != null) {
                                mStoredHomeChapterId = homeChapterId;
                                if (person.getCover() != null) {
                                    App.getInstance().getPicasso().load(person.getCover().getCoverPhoto().getUrl())
                                            .into(mDrawerImage);
                                }
                            }
                        }
                    })
                    .buildAndExecute();
        }
    }

    protected String getCurrentHomeChapterId() {
        return PrefUtils.getHomeChapterId(this);
    }

    protected boolean isHomeChapterOutdated(final String currentHomeChapterId) {
        return currentHomeChapterId != null && (mStoredHomeChapterId == null || !mStoredHomeChapterId.equals(currentHomeChapterId));
    }


    public static Person getPersonSync(final GoogleApiClient apiClient, final String gplusId) {
        final String cacheUrl = Const.CACHE_KEY_PERSON + gplusId;
        Object cachedPerson = App.getInstance().getModelCache().get(cacheUrl);

        if (cachedPerson instanceof Person) {
            return (Person) cachedPerson;
        }
        if (cachedPerson != null) {
            App.getInstance().getModelCache().remove(cacheUrl);
        }

        People.LoadPeopleResult result = Plus.PeopleApi.load(apiClient, gplusId).await();
        if (result.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = result.getPersonBuffer();
            try {
                if (personBuffer.getCount() > 0) {
                    Person person = personBuffer.get(0);
                    App.getInstance().getModelCache().put(cacheUrl, person, DateTime.now().plusDays(2));
                    return person;
                }
            } finally {
                personBuffer.close();
            }
        }
        return null;
    }
}
