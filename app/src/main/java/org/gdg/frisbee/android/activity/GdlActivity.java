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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.gms.cast.MediaInfo;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.viewpagerindicator.TitlePageIndicator;

import java.lang.ref.WeakReference;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.fragment.GdlListFragment;
import org.gdg.frisbee.android.utils.PlayServicesHelper;

import butterknife.InjectView;
/**
 * @author maui
 */
public class GdlActivity extends GdgNavDrawerActivity {

    private static String LOG_TAG = "GDG-GdlActivity";

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @InjectView(R.id.titles)
    TitlePageIndicator mIndicator;

    private GdlCategoryAdapter mViewPagerAdapter;
    private VideoCastManager mVideoCastManager;
    protected int mRequestedClients = PlayServicesHelper.CLIENT_GAMES | PlayServicesHelper.CLIENT_PLUS;
    private PlayServicesHelper mPlayerService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVideoCastManager = App.getVideoCastManager(this);

        mPlayerService = new PlayServicesHelper(this);
        mPlayerService.setup(this, mRequestedClients);
        setContentView(R.layout.activity_gdl);

        getSupportActionBar().setLogo(R.drawable.ic_gdl_logo_wide);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPagerAdapter = new GdlCategoryAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mIndicator.setViewPager(mViewPager);
    }

    protected String getTrackedViewName() {
        return "GDL/" + getResources().getStringArray(R.array.gdl_catgories)[getCurrentPage()];
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");

        try {
            mVideoCastManager = VideoCastManager.getInstance(this);
            mVideoCastManager.incrementUiCounter();
        } catch (CastException e) {
            e.printStackTrace();
        }
        //mIndicator.setOnPageChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
        mVideoCastManager.decrementUiCounter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gdl_menu, menu);
        mVideoCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    public void startCastControllerActivity(MediaInfo mediaInfo) {
        mVideoCastManager.startCastControllerActivity(this, mediaInfo, 0, true);
    }

    public boolean useCastDevice() {
        return mVideoCastManager != null && mVideoCastManager.isConnected();
    }

    public class GdlCategoryAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        private Context mContext;
        private final SparseArray<WeakReference<Fragment>> mFragments
                = new SparseArray<WeakReference<Fragment>>();

        public GdlCategoryAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public int getCount() {
            return mContext.getResources().getStringArray(R.array.gdl_catgories).length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = GdlListFragment.newInstance(mContext.getResources().getStringArray(R.array.gdl_catgories_url)[position], position == mViewPager.getCurrentItem());
            mFragments.append(position, new WeakReference<Fragment>(frag));

            return frag;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getResources().getStringArray(R.array.gdl_catgories)[position];
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int i) {
            WeakReference<Fragment> ref = mFragments.get(i);
            Fragment frag = null != ref ? ref.get() : null;

            // We need to notify the fragment that it is selected
            if (frag != null && frag instanceof Listener) {
                ((Listener) frag).onPageSelected();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }

    }

    public interface Listener {
        /**
         * Called when the item has been selected in the ViewPager.
         */
        void onPageSelected();
    }
}
