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

package org.gdg.frisbee.android.chapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AndroidAppUri;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.AppInviteDeepLinkActivity;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.OrganizerChecker;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.eventseries.GdgEventListFragment;
import org.gdg.frisbee.android.onboarding.FirstStartActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import timber.log.Timber;

public class MainActivity extends GdgNavDrawerActivity {

    public static final String TITLE_GOOGLE_DEVELOPER_GROUPS = "Google Developer Groups";
    private static final String SECTION_EVENTS = "events";
    private static final String ARG_SELECTED_CHAPTER = "selected_chapter";
    private static final String ARG_CHAPTERS = "chapters";
    private static final int[] PAGES = {
        R.string.news, R.string.info, R.string.events
    };
    private static final int[] ORGANIZER_PAGES = {
        R.string.news, R.string.info, R.string.events, R.string.for_leads
    };
    private static final int REQUEST_FIRST_START_WIZARD = 100;
    private static final int PLAY_SERVICE_DIALOG_REQUEST_CODE = 200;
    private static final Uri APP_URI =
        AndroidAppUri.newAndroidAppUri(BuildConfig.APPLICATION_ID, Uri.parse(Const.URL_GDGROUPS_ORG)).toUri();
    @Bind(R.id.pager)
    ViewPager mViewPager;
    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    private Handler mHandler = new Handler();
    ChapterAdapter mChapterAdapter;
    ChapterFragmentPagerAdapter mViewPagerAdapter;

    private boolean mFirstStart = false;

    private ChapterComparator mLocationComparator;
    private Spinner mSpinner;
    private String selectedChapterId;

    // Local Broadcast receiver for receiving invites
    @Nullable private BroadcastReceiver mDeepLinkReceiver;
    @Nullable private OrganizerCheckCallback organizerCheckCallback;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationComparator = new ChapterComparator(PrefUtils.getHomeChapterIdNotNull(this),
            App.getInstance().getLastLocation());

        mChapterAdapter = new ChapterAdapter(MainActivity.this, R.layout.spinner_item_actionbar);
        mChapterAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        initSpinner();

        ArrayList<Chapter> chapters = null;

        if (savedInstanceState != null) {
            chapters = savedInstanceState.getParcelableArrayList(ARG_CHAPTERS);
            selectedChapterId = savedInstanceState.getString(ARG_SELECTED_CHAPTER);
        } else {
            Intent intent = getIntent();

            if (FirstStartActivity.ACTION_FIRST_START.equals(intent.getAction())) {
                Timber.d("Completed FirstStartWizard");

                if (PrefUtils.isSignedIn(this)) {
                    mFirstStart = true;
                }
            }

            selectedChapterId = getChapterIdFromIntent(intent);

            if (AppInviteReferral.hasReferral(intent)) {
                // In this case the referral data is in the intent launching the MainActivity,
                // which means this user already had the app installed. We do not have to
                // register the Broadcast Receiver to listen for Play Store Install information
                launchAppInviteActivity(intent);
            }
        }

        if (selectedChapterId == null) {
            selectedChapterId = PrefUtils.getHomeChapterIdNotNull(this);
        }

        if (chapters != null) {
            initChapters(chapters);
        } else {
            App.getInstance().getModelCache().getAsync(
                Const.CACHE_KEY_CHAPTER_LIST_HUB,
                new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Directory directory = (Directory) item;
                        initChapters(directory.getGroups());
                    }

                    @Override
                    public void onNotFound(String key) {
                        if (Utils.isOnline(MainActivity.this)) {
                            fetchChapters();
                        } else {
                            showError(R.string.offline_alert);
                        }
                    }
                }
            );
        }

        if (PrefUtils.shouldShowSeasonsGreetings(this)) {
            SeasonsGreetingsFragment seasonsGreetings = new SeasonsGreetingsFragment();
            seasonsGreetings.show(getSupportFragmentManager(), "dialog");
        }

        if (PrefUtils.isSignedIn(this)) {
            checkAchievements();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int playServiceStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS != playServiceStatus) {
            GooglePlayServicesUtil.getErrorDialog(playServiceStatus, this, PLAY_SERVICE_DIALOG_REQUEST_CODE).show();
        }
        checkHomeChapterValid();
        updateChapterPages();
    }

    @Override
    protected void onPause() {
        if (organizerCheckCallback != null) {
            organizerCheckCallback.cancel();
            organizerCheckCallback = null;
        }
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        updateChapterPages();
    }

    private void updateChapterPages() {
        if (organizerCheckCallback != null) {
            organizerCheckCallback.cancel();
        }
        organizerCheckCallback = new OrganizerCheckCallback(isOrganizerFragmentShown());
        App.getInstance().checkOrganizer(
            getGoogleApiClient(),
            organizerCheckCallback
        );
    }

    private boolean isOrganizerFragmentShown() {
        return mViewPagerAdapter != null
            && mViewPagerAdapter.getCount() == ORGANIZER_PAGES.length;
    }

    private void checkHomeChapterValid() {
        String homeChapterId = getCurrentHomeChapterId();
        if (isHomeChapterOutdated(homeChapterId)
            && isShowingStoredHomeChapter()) {
            int position = mChapterAdapter.getPosition(homeChapterId);
            if (position != -1) {
                updateSelectionFor(homeChapterId);
            }
        }
    }

    private boolean isShowingStoredHomeChapter() {
        return selectedChapterId.equals(mStoredHomeChapterId);
    }

    private String getChapterIdFromIntent(final Intent intent) {
        if (intent.hasExtra(Const.EXTRA_CHAPTER_ID)) {
            return intent.getStringExtra(Const.EXTRA_CHAPTER_ID);
        } else if (intent.getData() != null
            && intent.getData().getScheme() != null
            && intent.getData().getScheme().equals("https")) {
            return intent.getData().getLastPathSegment();
        }
        return null;
    }

    void fetchChapters() {
        App.getInstance().getGdgXHub().getDirectory().enqueue(new Callback<Directory>() {
            @Override
            public void success(final Directory directory) {

                App.getInstance().getModelCache().putAsync(
                    Const.CACHE_KEY_CHAPTER_LIST_HUB,
                    directory,
                    DateTime.now().plusDays(1),
                    new ModelCache.CachePutListener() {
                        @Override
                        public void onPutIntoCache() {
                            ArrayList<Chapter> chapters = directory.getGroups();
                            initChapters(chapters);
                        }
                    });
            }

            @Override
            public void failure(Throwable error) {
                showError(R.string.fetch_chapters_failed);
            }

            @Override
            public void networkFailure(Throwable error) {
                showError(R.string.offline_alert);
            }
        });
    }

    /**
     * Initializes ViewPager, TabLayout, and Spinner Navigation.
     *
     * @param chapters Chapter array to be initialized, never null.
     */
    void initChapters(@NonNull ArrayList<Chapter> chapters) {
        addChapters(chapters);

        mViewPagerAdapter = new ChapterFragmentPagerAdapter(
            this,
            getSupportFragmentManager(), selectedChapterId
        );
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        updateSelectionFor(selectedChapterId);

        recordStartPageView();

        mTabLayout.setupWithViewPager(mViewPager);
        if (SECTION_EVENTS.equals(getIntent().getStringExtra(Const.EXTRA_SECTION))) {
            mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        mViewPager.setCurrentItem(2, true);
                    }
                }, 500
            );
        }
    }

    private void recordStartPageView() {
        Action viewAction = Action.newAction(Action.TYPE_VIEW, TITLE_GOOGLE_DEVELOPER_GROUPS, APP_URI);
        recordStartPageView(viewAction);
    }

    private void recordEndPageView() {
        Action viewAction = Action.newAction(Action.TYPE_VIEW, TITLE_GOOGLE_DEVELOPER_GROUPS, APP_URI);
        recordEndPageView(viewAction);
    }

    protected String getTrackedViewName() {
        if (mViewPager == null
            || mViewPagerAdapter == null) {
            return "Main";
        }
        final String[] pagesNames = {"News", "Info", "Events"};
        String pageName;
        try {
            pageName = pagesNames[getCurrentPage()];
        } catch (IndexOutOfBoundsException e) {
            pageName = "";
        }
        Chapter chapter = mChapterAdapter.findById(selectedChapterId);
        return "Main/" + (chapter != null ? chapter.getName().replaceAll(" ", "-") : "")
            + "/" + pageName;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
    }

    private void checkAchievements() {
        if (mFirstStart) {
            getAchievementActionHandler().handleSignIn();
        }
        getAchievementActionHandler().handleAppStarted();
        if (PrefUtils.isWidgetAdded(this)) {
            getAchievementActionHandler().handlePowerUser();
        }
    }

    private void addChapters(List<Chapter> chapterList) {
        Collections.sort(chapterList, mLocationComparator);
        mChapterAdapter.clear();
        mChapterAdapter.addAll(chapterList);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (PrefUtils.isFirstStart(this)) {
            startActivityForResult(new Intent(this, FirstStartActivity.class), REQUEST_FIRST_START_WIZARD);
        }
        registerDeepLinkReceiver();
    }

    @Override
    protected void onStop() {
        recordEndPageView();
        unregisterDeepLinkReceiver();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mChapterAdapter.getCount() > 0) {
            outState.putParcelableArrayList(ARG_CHAPTERS, mChapterAdapter.getAll());
        }
        if (mViewPagerAdapter != null) {
            outState.putString(ARG_SELECTED_CHAPTER, mViewPagerAdapter.getSelectedChapter());
        }
    }

    private void initSpinner() {
        Toolbar toolbar = getActionBarToolbar();
        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.actionbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );
        toolbar.addView(spinnerContainer, lp);

        mSpinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);

        mSpinner.setAdapter(mChapterAdapter);
        mSpinner.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> parent, final View view,
                                           final int position, final long id) {

                    Chapter selectedChapter = mChapterAdapter.getItem(position);
                    updateSelectionFor(selectedChapter.getGplusId());
                }

                @Override
                public void onNothingSelected(final AdapterView<?> parent) {
                    // Nothing to do.
                }
            }
        );
    }

    void updateSelectionFor(final String newChapterId) {
        mViewPagerAdapter.setSelectedChapter(newChapterId);
        mSpinner.setSelection(mChapterAdapter.getPosition(newChapterId));
        if (!selectedChapterId.equals(newChapterId)) {
            Timber.d("Switching newChapterId!");
            selectedChapterId = newChapterId;
            mViewPagerAdapter.notifyDataSetChanged(true /* forceUpdate */);
        }
    }

    private void registerDeepLinkReceiver() {
        // Create local Broadcast receiver that starts AppInviteDeepLinkActivity when a deep link
        // is found
        mDeepLinkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (AppInviteReferral.hasReferral(intent)) {
                    launchAppInviteActivity(intent);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(getString(R.string.action_deep_link));
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mDeepLinkReceiver, intentFilter
        );
    }

    private void unregisterDeepLinkReceiver() {
        if (mDeepLinkReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mDeepLinkReceiver);
        }
    }

    /**
     * Launch AppInviteActivity with an intent containing App Invite information
     */
    void launchAppInviteActivity(Intent intent) {
        Timber.d("launchAppInviteActivity:" + intent);
        Intent newIntent = new Intent(intent).setClass(this, AppInviteDeepLinkActivity.class);
        startActivity(newIntent);
    }

    public class ChapterFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private int[] mPages;
        private Context mContext;
        @NonNull
        private String mSelectedChapterId;
        private boolean forceUpdate = false;

        public ChapterFragmentPagerAdapter(Context ctx,
                                           FragmentManager fm,
                                           @NonNull String selectedChapterId) {
            super(fm);
            mContext = ctx;
            mSelectedChapterId = selectedChapterId;
            mPages = App.getInstance().isOrganizer() ? ORGANIZER_PAGES : PAGES;
        }

        public void notifyDataSetChanged(boolean forceUpdate) {
            this.forceUpdate = forceUpdate;
            notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged() {
            mPages = App.getInstance().isOrganizer() ? ORGANIZER_PAGES : PAGES;
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (forceUpdate) {
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

        @Override
        public int getCount() {
            return mPages.length;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return NewsFragment.newInstance(mSelectedChapterId);
                case 1:
                    return InfoFragment.newInstance(mSelectedChapterId);
                case 2:
                    return GdgEventListFragment.newInstance(mSelectedChapterId);
                case 3:
                    return LeadFragment.newInstance(mSelectedChapterId);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (0 <= position && position < mPages.length) {
                return mContext.getString(mPages[position]);
            } else {
                return "";
            }
        }

        @NonNull
        public String getSelectedChapter() {
            return mSelectedChapterId;
        }

        public void setSelectedChapter(@NonNull String chapterId) {
            trackView();
            mSelectedChapterId = chapterId;
        }
    }

    private class OrganizerCheckCallback implements OrganizerChecker.Callbacks {
        private final boolean wasOrganizer;
        private boolean cancelled = false;

        public OrganizerCheckCallback(boolean wasOrganizer) {
            this.wasOrganizer = wasOrganizer;
        }

        @Override
        public void onOrganizerResponse(boolean isOrganizer) {
            if (cancelled) {
                return;
            }
            if (mViewPagerAdapter != null && wasOrganizer != isOrganizer) {
                mViewPagerAdapter.notifyDataSetChanged(false /* forceUpdate */);
                mTabLayout.setTabsFromPagerAdapter(mViewPagerAdapter);
            }
        }

        @Override
        public void onErrorResponse() {
        }

        public void cancel() {
            cancelled = true;
        }
    }
}
