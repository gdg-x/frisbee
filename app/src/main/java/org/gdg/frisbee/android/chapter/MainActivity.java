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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AndroidAppUri;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.OrganizerChecker;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.onboarding.FirstStartActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import timber.log.Timber;

public class MainActivity extends GdgNavDrawerActivity implements ChapterSelectDialog.Listener {

    public static final String TITLE_GOOGLE_DEVELOPER_GROUPS = "Google Developer Groups";
    private static final String SECTION_EVENTS = "events";
    private static final String ARG_SELECTED_CHAPTER = "selected_chapter";
    private static final String ARG_CHAPTERS = "chapters";
    private static final int REQUEST_FIRST_START_WIZARD = 100;
    private static final Uri APP_URI =
        AndroidAppUri.newAndroidAppUri(BuildConfig.APPLICATION_ID, Uri.parse(Const.URL_GDGROUPS_ORG)).toUri();
    @BindView(R.id.pager)
    ViewPager viewPager;
    @BindView(R.id.tabs)
    TabLayout tabLayout;

    ChapterFragmentPagerAdapter mViewPagerAdapter;

    private ChapterComparator locationComparator;
    private TextView chapterSwitcher;
    private Chapter selectedChapter;
    private ArrayList<Chapter> chapters = new ArrayList<>();

    @Nullable
    private OrganizerChecker.Callbacks organizerCheckCallback;
    private Chapter homeChapter;

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

        homeChapter = PrefUtils.getHomeChapter(this);
        locationComparator = new ChapterComparator(homeChapter, App.getInstance().getLastLocation());

        if (savedInstanceState != null) {
            chapters = savedInstanceState.getParcelableArrayList(ARG_CHAPTERS);
            selectedChapter = savedInstanceState.getParcelable(ARG_SELECTED_CHAPTER);
        } else {
            String chapterId = getChapterIdFromIntent(getIntent());
            if (chapterId != null) {
                selectedChapter = new Chapter(chapterId);
            }
        }
        if (selectedChapter == null) {
            selectedChapter = homeChapter;
        }
        setupChapterSwitcher();

        if (!chapters.isEmpty()) {
            initUI();
        } else {
            loadChaptersFromCache();
        }

        if (PrefUtils.shouldShowSeasonsGreetings(this)) {
            SeasonsGreetingsFragment seasonsGreetings = new SeasonsGreetingsFragment();
            seasonsGreetings.show(getSupportFragmentManager(), "dialog");
        }
    }

    private void loadChaptersFromCache() {
        App.getInstance().getModelCache().getAsync(
            ModelCache.KEY_CHAPTER_LIST_HUB,
            new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    onDirectoryLoaded((Directory) item);
                }

                @Override
                public void onNotFound(String key) {
                    fetchChapters();
                }
            }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkHomeChapterValid();
        updateChapterPages();
    }

    @Override
    protected void onPause() {
        if (organizerCheckCallback != null) {
            organizerCheckCallback = null;
        }
        super.onPause();
    }

    private void checkHomeChapterValid() {
        Chapter newHomeChapter = PrefUtils.getHomeChapter(this);
        if (hasTheSameHomeChapter(newHomeChapter)) {
            return;
        }
        if (isShowingStoredHomeChapter()) {
            updateSelectionFor(newHomeChapter);
        }
        homeChapter = newHomeChapter;
    }

    private boolean hasTheSameHomeChapter(Chapter newHomeChapter) {
        return newHomeChapter == null || newHomeChapter.equals(homeChapter);
    }

    private boolean isShowingStoredHomeChapter() {
        return homeChapter.equals(selectedChapter);
    }

    private void updateChapterPages() {
        organizerCheckCallback = new OrganizerChecker.Callbacks() {
            @Override
            public void onOrganizerResponse(boolean isOrganizer) {
                if (isOrganizer != isOrganizerFragmentShown()) {
                    setupPagerWith(selectedChapter, isOrganizer);
                }
            }

            @Override
            public void onErrorResponse() {

            }
        };
        App.getInstance().checkOrganizer(organizerCheckCallback);
    }

    private boolean isOrganizerFragmentShown() {
        return mViewPagerAdapter != null && mViewPagerAdapter.isOrganizerFragmentShown();
    }

    private static String getChapterIdFromIntent(final Intent intent) {
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
                    ModelCache.KEY_CHAPTER_LIST_HUB,
                    directory,
                    DateTime.now().plusDays(1),
                    new ModelCache.CachePutListener() {
                        @Override
                        public void onPutIntoCache() {
                            onDirectoryLoaded(directory);
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

    private void onDirectoryLoaded(Directory directory) {
        chapters = directory.getGroups();
        Collections.sort(chapters, locationComparator);
        initUI();
    }

    void initUI() {
        setupPagerWith(selectedChapter, App.getInstance().isOrganizer());
        viewPager.setOffscreenPageLimit(3);

        tabLayout.setupWithViewPager(viewPager);
        if (SECTION_EVENTS.equals(getIntent().getStringExtra(Const.EXTRA_SECTION))) {
            viewPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewPager.setCurrentItem(2, true);
                }
            }, 500);
        }

        recordStartPageView();
    }

    private void setupPagerWith(Chapter selectedChapter, boolean isOrganizer) {
        mViewPagerAdapter = new ChapterFragmentPagerAdapter(this, selectedChapter, isOrganizer);
        viewPager.setAdapter(mViewPagerAdapter);
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
        if (viewPager == null
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
        return "Main/" + (selectedChapter != null ? selectedChapter.getName().replaceAll(" ", "-") : "")
            + "/" + pageName;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (PrefUtils.isFirstStart(this)) {
            startActivityForResult(new Intent(this, FirstStartActivity.class), REQUEST_FIRST_START_WIZARD);
        }
    }

    @Override
    protected void onStop() {
        recordEndPageView();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(ARG_CHAPTERS, chapters);
        outState.putParcelable(ARG_SELECTED_CHAPTER, selectedChapter);
    }

    private void setupChapterSwitcher() {
        Toolbar toolbar = getActionBarToolbar();
        View container = LayoutInflater.from(this).inflate(R.layout.actionbar_chapter_selector, toolbar);
        chapterSwitcher = (TextView) container.findViewById(android.R.id.text1);

        chapterSwitcher.setText(selectedChapter.toString());
        chapterSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChapterSelectDialog.newInstance(selectedChapter)
                    .show(getSupportFragmentManager(), ChapterSelectDialog.class.getSimpleName());
            }
        });
    }

    void updateSelectionFor(Chapter newChapter) {
        chapterSwitcher.setText(newChapter.toString());
        if (!newChapter.equals(selectedChapter)) {
            Timber.d("Switching newChapterId!");
            selectedChapter = newChapter;
            setupPagerWith(newChapter, isOrganizerFragmentShown());
        }
    }

    @Override
    public void onChapterSelected(Chapter selectedChapter) {
        updateSelectionFor(selectedChapter);
    }

}
