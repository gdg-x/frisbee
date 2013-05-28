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
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.actionbarsherlock.app.ActionBar;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.viewpagerindicator.TitlePageIndicator;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.GsonRequest;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.ApiException;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.fragment.EventFragment;
import org.gdg.frisbee.android.fragment.InfoFragment;
import org.gdg.frisbee.android.fragment.NewsFragment;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends GdgActivity implements android.app.ActionBar.OnNavigationListener {

    private static String LOG_TAG = "GDG-MainActivity";
    private ChapterAdapter mSpinnerAdapter;
    private MyAdapter mViewPagerAdapter;

    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    @InjectView(R.id.titles)
    private TitlePageIndicator mIndicator;

    private GroupDirectory.ApiRequest mFetchChaptersTask;

    private LocationManager mLocationManager;
    private GroupDirectory mClient;

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
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //if(!Utils.isEmulator())
        //    Log.d(LOG_TAG, mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).toString());

        mViewPagerAdapter = new MyAdapter(this, getSupportFragmentManager());
        mSpinnerAdapter = new ChapterAdapter(MainActivity.this, android.R.layout.simple_list_item_1);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setListNavigationCallbacks(mSpinnerAdapter, MainActivity.this);

        mFetchChaptersTask = mClient.getDirectory(new Response.Listener<Directory>() {
            @Override
            public void onResponse(Directory directory) {
                getActionBar().setListNavigationCallbacks(mSpinnerAdapter, MainActivity.this);
                App.getInstance().getModelCache().putAsync("chapter_list", directory, DateTime.now().plusDays(1));

                ArrayList<Chapter> chapters = directory.getGroups();
                Collections.sort(chapters);
                mSpinnerAdapter.addAll(chapters);

                mViewPagerAdapter.setSelectedChapter(chapters.get(0));

                mViewPager.setAdapter(mViewPagerAdapter);
                mIndicator.setViewPager(mViewPager);
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
                        if(directory != null) {
                            mSpinnerAdapter.addAll(directory.getGroups());

                            mViewPagerAdapter.setSelectedChapter(directory.getGroups().get(0));
                            mViewPager.setAdapter(mViewPagerAdapter);
                            mIndicator.setViewPager(mViewPager);
                        } else {
                            mFetchChaptersTask.execute();
                        }
                    }
                });
            } else {

                App.getInstance().getModelCache().getAsync("chapter_list", false, new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Directory directory = (Directory)item;
                        if(directory != null) {
                            mSpinnerAdapter.addAll(directory.getGroups());
                            mViewPagerAdapter.setSelectedChapter(directory.getGroups().get(0));
                            mViewPager.setAdapter(mViewPagerAdapter);
                            mIndicator.setViewPager(mViewPager);
                        } else {
                            Crouton.makeText(MainActivity.this, getString(R.string.offline_alert), Style.ALERT).show();
                        }
                    }
                });
            }
        } else {

            if(savedInstanceState.containsKey("chapters")) {
                ArrayList<Chapter> chapters = savedInstanceState.getParcelableArrayList("chapters");
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
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
        if(!previous.equals(mSpinnerAdapter.getItem(position))) {
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
            this.mSelectedChapter = chapter;
        }
    }
}

