/*
 * Copyright 2013-2015 The GDG Frisbee Project
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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.fragment.PulseFragment;
import org.gdg.frisbee.android.view.SlidingTabLayout;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

public class PulseActivity extends GdgNavDrawerActivity implements PulseFragment.Pulse {

    @InjectView(R.id.pager)
    ViewPager mViewPager;
    @InjectView(R.id.sliding_tabs)
    SlidingTabLayout mSlidingTabLayout;
    private ArrayAdapter<String> mSpinnerAdapter;
    private MyAdapter mViewPagerAdapter;
    private ArrayList<String> mPulseTargets;
    private ApiRequest mFetchGlobalPulseTask;
    private Spinner mSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.i("onCreate");
        setContentView(R.layout.activity_pulse);

        final GroupDirectory mClient = new GroupDirectory();

        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setOnPageChangeListener(this);

        mPulseTargets = new ArrayList<>();

        mViewPagerAdapter = new MyAdapter(this, getSupportFragmentManager());
        mSpinnerAdapter = new ArrayAdapter<>(PulseActivity.this, android.R.layout.simple_list_item_1);

        mFetchGlobalPulseTask = mClient.getPulse(
                new Response.Listener<Pulse>() {
                    @Override
                    public void onResponse(final Pulse pulse) {
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
                        Timber.e("Couldn't fetch chapter list", volleyError);
                    }
                }
        );

        App.getInstance().getModelCache().getAsync("pulse_global", true, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Pulse pulse = (Pulse) item;
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

        return "Pulse/" + mViewPagerAdapter.getSelectedPulseTarget().replaceAll(" ", "-") +
                "/" + pageName;
    }

    private void initSpinner() {
        Toolbar toolbar = getActionBarToolbar();
        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.actionbar_spinner,
                toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        mSpinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                String previous = mViewPagerAdapter.getSelectedPulseTarget();
                mViewPagerAdapter.setSelectedPulseTarget(mSpinnerAdapter.getItem(position));
                if (!previous.equals(mSpinnerAdapter.getItem(position))) {
                    Timber.d("Switching chapter!");
                    mViewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // Nothing to do.
            }
        });

        Collections.sort(mPulseTargets);
        mPulseTargets.add(0, "Global");
        mViewPagerAdapter.setSelectedPulseTarget(mPulseTargets.get(0));
        mSpinnerAdapter.clear();

        mSpinnerAdapter.addAll(mPulseTargets);

        mViewPager.setAdapter(mViewPagerAdapter);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    public ArrayList<String> getPulseTargets() {
        return mPulseTargets;
    }

    @Override
    public void openPulse(final String key) {
        mSpinner.setSelection(getPulseTargets().indexOf(key));
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
            if (mSelectedPulseTarget == null)
                return 0;
            else
                return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
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
            switch (position) {
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
            if (mSelectedPulseTarget != null)
                trackView();

            mSelectedPulseTarget = pulseTarget;
        }
    }
}
