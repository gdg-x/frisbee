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

package org.gdg.frisbee.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.eventseries.GdgEventListFragment;
import org.gdg.frisbee.android.fragment.InfoFragment;
import org.gdg.frisbee.android.fragment.LeadFragment;
import org.gdg.frisbee.android.fragment.NewsFragment;
import org.gdg.frisbee.android.fragment.SeasonsGreetingsFragment;
import org.gdg.frisbee.android.utils.ChapterComparator;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.SlidingTabLayout;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import timber.log.Timber;

public class MainActivity extends GdgNavDrawerActivity {

    public static final String SECTION_EVENTS = "events";
    private static final String ARG_SELECTED_CHAPTER = "selected_chapter";
    private static final String ARG_CHAPTERS = "chapters";

    public static final int REQUEST_FIRST_START_WIZARD = 100;
    private static final int PLAY_SERVICE_DIALOG_REQUEST_CODE = 200;

    @InjectView(R.id.pager)
    ViewPager mViewPager;
    @InjectView(R.id.sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;
    private Handler mHandler = new Handler();
    private ChapterAdapter mChapterAdapter;
    private ChapterFragmentPagerAdapter mViewPagerAdapter;

    private boolean mFirstStart = false;

    private ChapterComparator mLocationComparator;
    private Spinner mSpinner;

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

        mLocationComparator = new ChapterComparator(PrefUtils.getHomeChapterIdNotNull(this));

        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setOnPageChangeListener(this);

        mChapterAdapter = new ChapterAdapter(MainActivity.this, true /* white text */);
        initSpinner();

        if (savedInstanceState == null) {

            Intent intent = getIntent();
            if (intent != null
                    && intent.getAction() != null
                    && intent.getAction().equals(FirstStartActivity.ACTION_FIRST_START)) {
                Timber.d("Completed FirstStartWizard");

                if (PrefUtils.isSignedIn(this)) {
                    mFirstStart = true;
                }
            }

            App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB, new ModelCache.CacheListener() {
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
                        Crouton.makeText(MainActivity.this, getString(R.string.offline_alert), Style.ALERT).show();
                    }
                }
            });

        } else {
            if (savedInstanceState.containsKey(ARG_CHAPTERS)) {
                ArrayList<Chapter> chapters = savedInstanceState.getParcelableArrayList(ARG_CHAPTERS);

                Chapter selectedChapter = savedInstanceState.getParcelable(ARG_SELECTED_CHAPTER);
                initChapters(chapters, selectedChapter);
            } else {
                fetchChapters();
            }
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
    }

    private void checkHomeChapterValid() {
        String homeChapterId = getCurrentHomeChapterId();
        if (isHomeChapterOutdated(homeChapterId)
                && isShowingStoredHomeChapter()) {
            Chapter chapter = mChapterAdapter.findById(homeChapterId);
            if (chapter != null) {
                updateSelectionfor(chapter);
            }
        }
    }

    private boolean isShowingStoredHomeChapter() {
        if (mStoredHomeChapterId == null) {
            return false;
        }
        Chapter chapterShown = mViewPagerAdapter.getSelectedChapter();
        return chapterShown != null && chapterShown.getGplusId().equals(mStoredHomeChapterId);
    }

    /**
     * Initializes ViewPager, SlidingTabLayout, and Spinner Navigation.
     * <p/>
     * It tries to get selected chapter from Intent Extras,
     * if not found, gets the first Chapter in the array.
     * This function is called after cache query or network call is success
     *
     * @param chapters Chapter array to be initialized, never null.
     */
    private void initChapters(@NonNull ArrayList<Chapter> chapters) {

        Chapter selectedChapter = null;
        if (getIntent().hasExtra(Const.EXTRA_CHAPTER_ID)) {
            final String chapterId = getIntent().getStringExtra(Const.EXTRA_CHAPTER_ID);
            for (Chapter c : chapters) {
                if (c.getGplusId().equals(chapterId)) {
                    selectedChapter = c;
                    break;
                }
            }
        }

        if (selectedChapter == null) {
            selectedChapter = chapters.get(0);
        }
        initChapters(chapters, selectedChapter);
    }

    public void fetchChapters() {
        App.getInstance().getGdgXHub().getDirectory(new Callback<Directory>() {

            public void success(final Directory directory, retrofit.client.Response response) {
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

            public void failure(RetrofitError error) {
                try {
                    Crouton.makeText(MainActivity.this, R.string.fetch_chapters_failed, Style.ALERT).show();
                } catch (IllegalStateException exception) {
                }
                Timber.e(error, "Couldn't fetch chapter list");
            }
        });
    }

    /**
     * Initializes ViewPager, SlidingTabLayout, and Spinner Navigation.
     * This function is called after cache query or network call is success,
     * or after an orientation change using savedInstance.
     *
     * @param chapters        Chapter array to be initialized, never null.
     * @param selectedChapter selectedChapter, never null.
     */
    private void initChapters(@NonNull ArrayList<Chapter> chapters,
                              @NonNull Chapter selectedChapter) {
        addChapters(chapters);
        mViewPagerAdapter = new ChapterFragmentPagerAdapter(this,
                getSupportFragmentManager(), selectedChapter);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        updateSelectionfor(selectedChapter);

        mSlidingTabLayout.setViewPager(mViewPager);

        if (SECTION_EVENTS.equals(getIntent().getStringExtra(Const.EXTRA_SECTION))) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(2, true);
                }
            }, 500);
        }
    }

    protected String getTrackedViewName() {
        if (mViewPager == null
                || mViewPagerAdapter == null
                || mViewPagerAdapter.getSelectedChapter() == null) {
            return "Main";
        }
        final String[] pagesNames = {"News", "Info", "Events"};
        String pageName;
        try {
            pageName = pagesNames[getCurrentPage()];
        } catch (IndexOutOfBoundsException e) {
            pageName = "";
        }
        return "Main/" + mViewPagerAdapter.getSelectedChapter().getName().replaceAll(" ", "-")
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mChapterAdapter.getCount() > 0) {
            outState.putParcelableArrayList(ARG_CHAPTERS, mChapterAdapter.getAll());
        }
        if (mViewPagerAdapter != null && mViewPagerAdapter.getSelectedChapter() != null) {
            outState.putParcelable(ARG_SELECTED_CHAPTER, mViewPagerAdapter.getSelectedChapter());
        }
    }

    private void initSpinner() {
        Toolbar toolbar = getActionBarToolbar();
        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.actionbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        mSpinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        mSpinner.setAdapter(mChapterAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {

                Chapter selectedChapter = mChapterAdapter.getItem(position);
                updateSelectionfor(selectedChapter);
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // Nothing to do.
            }
        });
    }

    private void updateSelectionfor(final Chapter chapter) {
        Chapter previous = mViewPagerAdapter.getSelectedChapter();
        mViewPagerAdapter.setSelectedChapter(chapter);
        mSpinner.setSelection(mChapterAdapter.getPosition(chapter));
        if (previous == null || !previous.equals(chapter)) {
            Timber.d("Switching chapter!");
            mViewPagerAdapter.notifyDataSetChanged();
        }
    }

    public class ChapterFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private Chapter mSelectedChapter;

        public ChapterFragmentPagerAdapter(Context ctx, FragmentManager fm, @NonNull Chapter selectedChapter) {
            super(fm);
            mContext = ctx;
            mSelectedChapter = selectedChapter;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if (App.getInstance().isOrganizer()) {
                return 4;
            } else {
                return 3;
            }
        }

        @Override
        public Fragment getItem(int position) {
            String gplusId = mSelectedChapter == null ? "" : mSelectedChapter.getGplusId();
            switch (position) {
                case 0:
                    return NewsFragment.newInstance(gplusId);
                case 1:
                    return InfoFragment.newInstance(gplusId);
                case 2:
                    return GdgEventListFragment.newInstance(gplusId);
                case 3:
                    return LeadFragment.newInstance(gplusId);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mContext.getText(R.string.news);
                case 1:
                    return mContext.getText(R.string.info);
                case 2:
                    return mContext.getText(R.string.events);
                case 3:
                    return mContext.getText(R.string.for_leads);
            }
            return "";
        }

        public Chapter getSelectedChapter() {
            return mSelectedChapter;
        }

        public void setSelectedChapter(@NonNull Chapter chapter) {
            if (mSelectedChapter != null) {
                trackView();
            }
            mSelectedChapter = chapter;
        }
    }
}

