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
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.games.GamesClient;
import com.viewpagerindicator.TitlePageIndicator;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.adapter.DrawerAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GoogleDevelopersLive;
import org.gdg.frisbee.android.api.model.GdlShow;
import org.gdg.frisbee.android.api.model.GdlShowList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.fragment.EventFragment;
import org.gdg.frisbee.android.fragment.InfoFragment;
import org.gdg.frisbee.android.fragment.NewsFragment;
import org.gdg.frisbee.android.utils.ChapterComparator;
import org.gdg.frisbee.android.utils.GingerbreadLastLocationFinder;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ActionBarDrawerToggleCompat;
import org.joda.time.DateTime;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends GdgActivity implements ActionBar.OnNavigationListener {

    private static String LOG_TAG = "GDG-MainActivity";

    public static final int REQUEST_FIRST_START_WIZARD = 100;

    @InjectView(R.id.drawer)
    private DrawerLayout mDrawerLayout;

    @InjectView(R.id.left_drawer)
    private ListView mDrawerContent;

    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    @InjectView(R.id.titles)
    private TitlePageIndicator mIndicator;

    private DrawerAdapter mDrawerAdapter;
    private ChapterAdapter mSpinnerAdapter;
    private MyAdapter mViewPagerAdapter;
    private ActionBarDrawerToggleCompat mDrawerToggle;
    private ApiRequest mFetchChaptersTask;
    private SharedPreferences mPreferences;
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

        mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);

        mClient = new GroupDirectory();

        mLocationComparator = new ChapterComparator(mPreferences);

        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onPageSelected(int i) {
                Log.d(LOG_TAG, "onPageSelected()");
                trackViewPagerPage(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        mDrawerAdapter = new DrawerAdapter(this);
        mDrawerContent.setAdapter(mDrawerAdapter);
        mDrawerContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DrawerAdapter.DrawerItem item = (DrawerAdapter.DrawerItem) mDrawerAdapter.getItem(i);

                switch(item.getTitle()) {
                    case R.string.achievements:
                        getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                            @Override
                            public void onGotGamesClient(GamesClient c) {
                                startActivityForResult(c.getAchievementsIntent(), 0);
                            }
                        });
                        break;
                    case R.string.about:
                        startActivity(new Intent(MainActivity.this, AboutActivity.class));
                        break;
                    case R.string.gdl:
                        startActivity(new Intent(MainActivity.this, GdlActivity.class));
                        break;
                    case R.string.pulse:
                        startActivity(new Intent(MainActivity.this, PulseActivity.class));
                        break;
                    case R.string.settings:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                }
            }
        });

        mViewPagerAdapter = new MyAdapter(this, getSupportFragmentManager());
        mSpinnerAdapter = new ChapterAdapter(MainActivity.this, android.R.layout.simple_list_item_1);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, MainActivity.this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggleCompat(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

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
                    mViewPagerAdapter.setSelectedChapter(selectedChapter);
                    getSupportActionBar().setSelectedNavigationItem(mSpinnerAdapter.getPosition(selectedChapter));
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

    private void initChapters(ArrayList<Chapter> chapters) {
        addChapters(chapters);
        Chapter chapter = null;

        if(getIntent().hasExtra("org.gdg.frisbee.CHAPTER")) {
            String chapterId = getIntent().getStringExtra("org.gdg.frisbee.CHAPTER");
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
        mIndicator.setViewPager(mViewPager);
    }

    private void trackViewPagerPage(int position) {
        if(mViewPager == null || mViewPagerAdapter.getSelectedChapter() == null)
            return;

        Log.d(LOG_TAG, "trackViewPagerPage()");
        String page = "";

        switch(position) {
            case 0:
                page = "News";
                break;
            case 1:
                page = "Info";
                break;
            case 2:
                page = "Events";
                break;
        }
        App.getInstance().getTracker().sendView(String.format("/Main/%s/%s", mViewPagerAdapter.getSelectedChapter().getName().replaceAll(" ","-"), page));
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
        if(mFirstStart) {
            mFirstStart = false;
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                        @Override
                        public void onGotGamesClient(GamesClient c) {
                            c.unlockAchievement(Const.ACHIEVEMENT_SIGNIN);
                        }
                    });
                }
            }, 1000);
        }

        if(mPreferences.getInt(Const.SETTINGS_APP_STARTS,0) == 10) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                        @Override
                        public void onGotGamesClient(GamesClient c) {
                            c.unlockAchievement(Const.ACHIEVEMENT_RETURN);
                        }
                    });
                }
            }, 1000);
        }

        if(mPreferences.getInt(Const.SETTINGS_APP_STARTS,0) == 50) {
            getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                        @Override
                        public void onGotGamesClient(GamesClient c) {
                            c.unlockAchievement(Const.ACHIEVEMENT_KING_OF_THE_HILL);
                        }
                    });
                }
            }, 1000);
        }
    }

    private void addChapters(List<Chapter> chapterList) {
        Collections.sort(chapterList, mLocationComparator);
        mSpinnerAdapter.clear();
        mSpinnerAdapter.addAll(chapterList);
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
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
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");

        trackViewPagerPage(mViewPager.getCurrentItem());
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
                trackViewPagerPage(mViewPager.getCurrentItem());

            this.mSelectedChapter = chapter;
        }
    }
}

