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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.Collections;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.fragment.PulseFragment;
import org.joda.time.DateTime;

import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

public class PulseActivity extends GdgNavDrawerActivity implements ActionBar.OnNavigationListener {

    private static String LOG_TAG = "GDG-PulseActivity";
    private GroupDirectory mClient;

    private ArrayAdapter<String> mSpinnerAdapter;

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @InjectView(R.id.titles)
    TitlePageIndicator mIndicator;

    private MyAdapter mViewPagerAdapter;
    private ArrayList<String> mPulseTargets;
    private ApiRequest mFetchGlobalPulseTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.i("onCreate");
        setContentView(R.layout.activity_pulse);

        getSupportActionBar().setLogo(R.drawable.ic_logo_pulse);

        mClient = new GroupDirectory();

        mIndicator.setOnPageChangeListener(this);

        mPulseTargets = new ArrayList<String>();

        mViewPagerAdapter = new MyAdapter(this, getSupportFragmentManager());
        mSpinnerAdapter = new ArrayAdapter<String>(PulseActivity.this, android.R.layout.simple_list_item_1);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, PulseActivity.this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mFetchGlobalPulseTask = mClient.getPulse(new Response.Listener<Pulse>() {
                @Override
                public void onResponse(final Pulse pulse) {
                    getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, PulseActivity.this);
                    App.getInstance().getModelCache().putAsync("pulse_global", pulse, DateTime.now().plusDays(1), new ModelCache.CachePutListener() {
                         @Override
                         public void onPutIntoCache() {
                            mPulseTargets.addAll(pulse.keySet());
                            initSpinner();
                         }
                    });
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Crouton.makeText(PulseActivity.this, getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                    Timber.e("Could'nt fetch chapter list", volleyError);
                }
            }
        );

        App.getInstance().getModelCache().getAsync("pulse_global", true, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Pulse pulse = (Pulse)item;
                mPulseTargets.addAll(pulse.keySet());
                initSpinner();
            }

            @Override
            public void onNotFound(String key) {
                mFetchGlobalPulseTask.execute();
            }
        });
    }

    protected String getTrackedViewName() {
        if (mViewPager == null || mViewPagerAdapter.getSelectedPulseTarget() == null)
            return "Pulse";

        final String[] pagesNames = {"EventStats", "AtendeeStats", "CircleStats"};
        String pageName;
        try {
            pageName = pagesNames[getCurrentPage()];
        } catch (IndexOutOfBoundsException e) {
            pageName = "";
        }

        return "Pulse/"+mViewPagerAdapter.getSelectedPulseTarget().replaceAll(" ", "-") +
                "/" + pageName;
    }

    private void initSpinner() {
        Collections.sort(mPulseTargets);
        mPulseTargets.add(0,"Global");
        mViewPagerAdapter.setSelectedPulseTarget(mPulseTargets.get(0));
        mSpinnerAdapter.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mSpinnerAdapter.addAll(mPulseTargets);
        } else {
            for (String item : mPulseTargets) {
                mSpinnerAdapter.add(item);
            }
        }
        mViewPager.setAdapter(mViewPagerAdapter);
        mIndicator.setViewPager(mViewPager);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        String previous = mViewPagerAdapter.getSelectedPulseTarget();
        getSupportActionBar().setSelectedNavigationItem(itemPosition);
        mViewPagerAdapter.setSelectedPulseTarget(mSpinnerAdapter.getItem(itemPosition));
        if(!previous.equals(mSpinnerAdapter.getItem(itemPosition))) {
            Timber.d("Switching chapter!");
            mViewPagerAdapter.notifyDataSetChanged();
        }
        return true;
    }

    public ArrayAdapter<String> getSpinnerAdapter() {
        return mSpinnerAdapter;
    }

    public ArrayList<String> getPulseTargets() {
        return mPulseTargets;
    }

    public class MyAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private String mSelectedPulseTarget;

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
            if(mSelectedPulseTarget == null)
                return 0;
            else
                return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0:
                    return PulseFragment.newInstance(0, mSelectedPulseTarget);
                case 1:
                    return PulseFragment.newInstance(1, mSelectedPulseTarget);
                case 2:
                    return PulseFragment.newInstance(2, mSelectedPulseTarget);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0:
                    return mContext.getText(R.string.pulse_events);
                case 1:
                    return mContext.getText(R.string.pulse_attendees);
                case 2:
                    return mContext.getText(R.string.pulse_circlers);
            }
            return "";
        }

        public String getSelectedPulseTarget() {
            return mSelectedPulseTarget;
        }

        public void setSelectedPulseTarget(String pulseTarget) {
            if(mSelectedPulseTarget != null)
                trackView();

            this.mSelectedPulseTarget = pulseTarget;
        }
    }
}
