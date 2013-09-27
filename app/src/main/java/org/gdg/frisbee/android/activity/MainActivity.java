/*
 * Copyright 2013 The GDG Frisbee Project
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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.games.GamesClient;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.fragment.EventFragment;
import org.gdg.frisbee.android.fragment.InfoFragment;
import org.gdg.frisbee.android.fragment.NewsFragment;
import org.gdg.frisbee.android.utils.ChapterComparator;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import roboguice.inject.InjectView;

public class MainActivity extends GdgNavDrawerActivity implements ActionBar.OnNavigationListener{

    public static final String EXTRA_GROUP_ID = "org.gdg.frisbee.CHAPTER";
    public static final String SECTION_EVENTS = "events";
    public static final String EXTRA_SECTION = "org.gdg.frisbee.SECTION";
    public static final String EXTRA_EVENT_ID = "org.gdg.frisbee.EVENT";

    private static String LOG_TAG = "GDG-MainActivity";

    public static final int REQUEST_FIRST_START_WIZARD = 100;

    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    @InjectView(R.id.titles)
    protected TitlePageIndicator mIndicator;

    private Handler mHandler = new Handler();

    private ChapterAdapter mSpinnerAdapter;
    private MyAdapter mViewPagerAdapter;
    private ApiRequest mFetchChaptersTask;
    private LocationManager mLocationManager;
    private GroupDirectory mClient;

    private boolean mFirstStart = false;

    private ChapterComparator mLocationComparator;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after 
     * previously being shut down then this Bundle contains the data it most 
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_main);

        mClient = new GroupDirectory();

        mLocationComparator = new ChapterComparator(mPreferences);

        mIndicator.setOnPageChangeListener(this);

        mViewPagerAdapter = new MyAdapter(this, getSupportFragmentManager());
        mSpinnerAdapter = new ChapterAdapter(MainActivity.this, android.R.layout.simple_list_item_1);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, MainActivity.this);



        mFetchChaptersTask = mClient.getDirectory(new Response.Listener<Directory>() {
            @Override
            public void onResponse(final Directory directory) {
                getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, MainActivity.this);
                App.getInstance().getModelCache().putAsync("chapter_list", directory, DateTime.now().plusDays(1), new ModelCache.CachePutListener() {
                    @Override
                    public void onPutIntoCache() {
                        ArrayList<Chapter> chapters = directory.getGroups();

                        initChapters(chapters);
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Crouton.makeText(MainActivity.this, getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                Log.e(LOG_TAG, "Could'nt fetch chapter list", volleyError);
            }
        });

        if(savedInstanceState == null) {

            if(Utils.isOnline(this)) {
                App.getInstance().getModelCache().getAsync("chapter_list", new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Directory directory = (Directory)item;
                        initChapters(directory.getGroups());
                    }

                    @Override
                    public void onNotFound(String key) {
                        mFetchChaptersTask.execute();
                    }
                });
            } else {

                App.getInstance().getModelCache().getAsync("chapter_list", false, new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Directory directory = (Directory)item;
                        initChapters(directory.getGroups());
                    }

                    @Override
                    public void onNotFound(String key) {
                        Crouton.makeText(MainActivity.this, getString(R.string.offline_alert), Style.ALERT).show();
                    }
                });
            }
        } else {

            if(savedInstanceState.containsKey("chapters")) {
                ArrayList<Chapter> chapters = savedInstanceState.getParcelableArrayList("chapters");
                mSpinnerAdapter.clear();
                mSpinnerAdapter.addAll(chapters);

                if(savedInstanceState.containsKey("selected_chapter")) {
                    Chapter selectedChapter = savedInstanceState.getParcelable("selected_chapter");
                    selectChapter(selectedChapter);
                } else {
                    mViewPagerAdapter.setSelectedChapter(chapters.get(0));
                }

                mViewPager.setAdapter(mViewPagerAdapter);
                mIndicator.setViewPager(mViewPager);
            } else {
                mFetchChaptersTask.execute();
            }
        }

        Intent intent = getIntent();
        if(intent != null && intent.getAction() != null && intent.getAction().equals("finish_first_start")) {
                Log.d(LOG_TAG, "Completed FirstStartWizard");

                if(mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
                    mFirstStart = true;
                }

                Chapter homeGdgd = getIntent().getParcelableExtra("selected_chapter");
                getSupportActionBar().setSelectedNavigationItem(mSpinnerAdapter.getPosition(homeGdgd));
                mViewPagerAdapter.setSelectedChapter(homeGdgd);
        }
    }

    private void selectChapter(Chapter chapter) {
        mViewPagerAdapter.setSelectedChapter(chapter);
        getSupportActionBar().setSelectedNavigationItem(mSpinnerAdapter.getPosition(chapter));
    }

    private void initChapters(ArrayList<Chapter> chapters) {
        addChapters(chapters);
        Chapter chapter = null;

        if(getIntent().hasExtra(EXTRA_GROUP_ID)) {
            String chapterId = getIntent().getStringExtra(EXTRA_GROUP_ID);
            for(Chapter c : chapters) {
                if(c.getGplusId().equals(chapterId)) {
                    chapter = c;
                    break;
                }
            }
            if(chapter == null)
                chapter = chapters.get(0);
        } else {
            chapter = chapters.get(0);
        }

        getSupportActionBar().setSelectedNavigationItem(mSpinnerAdapter.getPosition(chapter));
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mIndicator.setViewPager(mViewPager);

        if (SECTION_EVENTS.equals(getIntent().getStringExtra(EXTRA_SECTION))) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(2, true);
                }
            },500);
        }
    }

    protected String getTrackedViewName() {
        if(mViewPager == null || mViewPagerAdapter.getSelectedChapter() == null)
            return "Main";
        final String[] pagesNames = {"News", "Info", "Events"};
        String pageName;
        try {
            pageName = pagesNames[getCurrentPage()];
        } catch (IndexOutOfBoundsException e) {
            pageName = "";
        }
        return "Main/" + mViewPagerAdapter.getSelectedChapter().getName().replaceAll(" ", "-")+
                "/" + pageName;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
    }

    @Override
    public void onSignInFailed() {
        super.onSignInFailed();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onSignInSucceeded() {
        super.onSignInSucceeded();    //To change body of overridden methods use File | Settings | File Templates.

        checkAchievements();
    }

    private void checkAchievements() {
        if (mFirstStart)
            getAchievementActionHandler().handleSignIn();
        getAchievementActionHandler().handleAppStarted();
    }


    private void addChapters(List<Chapter> chapterList) {
        Collections.sort(chapterList, mLocationComparator);
        mSpinnerAdapter.clear();
        mSpinnerAdapter.addAll(chapterList);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()");

        if(mPreferences.getBoolean(Const.SETTINGS_FIRST_START, Const.SETTINGS_FIRST_START_DEFAULT)) {
            startActivityForResult(new Intent(this, FirstStartActivity.class), REQUEST_FIRST_START_WIZARD);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mSpinnerAdapter.getCount() > 0)
            outState.putParcelableArrayList("chapters", mSpinnerAdapter.getAll());
        if(mViewPagerAdapter.getSelectedChapter() != null)
            outState.putParcelable("selected_chapter", mViewPagerAdapter.getSelectedChapter());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long l) {
        Chapter previous = mViewPagerAdapter.getSelectedChapter();
        mViewPagerAdapter.setSelectedChapter(mSpinnerAdapter.getItem(position));
        if(previous == null || !previous.equals(mSpinnerAdapter.getItem(position))) {
            Log.d(LOG_TAG, "Switching chapter!");
            mViewPagerAdapter.notifyDataSetChanged();
        }
        return true;
    }

    public class MyAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private Chapter mSelectedChapter;

        public MyAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            if(mSelectedChapter == null)
                return 0;
            else
                return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return NewsFragment.newInstance(mSelectedChapter.getGplusId());
                case 1:
                    return InfoFragment.newInstance(mSelectedChapter.getGplusId());
                case 2:
                    return EventFragment.newInstance(mSelectedChapter.getGplusId());
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return mContext.getText(R.string.news);
                case 1:
                    return mContext.getText(R.string.info);
                case 2:
                    return mContext.getText(R.string.events);
            }
            return "";
        }

        public Chapter getSelectedChapter() {
            return mSelectedChapter;
        }

        public void setSelectedChapter(Chapter chapter) {
            if(mSelectedChapter != null)
                trackView();

            this.mSelectedChapter = chapter;
        }
    }
}

