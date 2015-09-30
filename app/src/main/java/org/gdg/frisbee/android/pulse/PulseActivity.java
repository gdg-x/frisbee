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

package org.gdg.frisbee.android.pulse;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
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
import android.widget.FrameLayout;
import android.widget.Spinner;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;

public class PulseActivity extends GdgNavDrawerActivity implements PulseFragment.Callbacks {

    private static final String INSTANCE_STATE_SELECTED_PULSE = "INSTANCE_STATE_SELECTED_PULSE";
    @Bind(R.id.pager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    @Bind(R.id.content_frame)
    FrameLayout mContentLayout;

    private ArrayAdapter<String> mSpinnerAdapter;
    private PulsePagerAdapter mViewPagerAdapter;
    private ArrayList<String> mPulseTargets;
    private Spinner mSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pulse);

        mPulseTargets = new ArrayList<>();
        mViewPagerAdapter = new PulsePagerAdapter(this, getSupportFragmentManager());

        final String selectedPulse = savedInstanceState != null ? savedInstanceState.getString(INSTANCE_STATE_SELECTED_PULSE) : null;

        App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_PULSE_GLOBAL, true, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Pulse pulse = (Pulse) item;
                mPulseTargets.addAll(pulse.keySet());
                initSpinner(selectedPulse);
            }

            @Override
            public void onNotFound(String key) {
                fetchPulse(selectedPulse);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mSpinner != null && !PulseFragment.GLOBAL.equals(mSpinner.getSelectedItem())) {
            openPulse(PulseFragment.GLOBAL);
            return;
        }
        super.onBackPressed();
    }

    private void fetchPulse(final String selectedPulse) {
        App.getInstance().getGroupDirectory().getPulse().enqueue(new Callback<Pulse>() {
            @Override
            public void success(final Pulse pulse) {
                App.getInstance().getModelCache().putAsync(
                        Const.CACHE_KEY_PULSE_GLOBAL,
                        pulse,
                        DateTime.now().plusDays(1),
                        new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                mPulseTargets.addAll(pulse.keySet());
                                initSpinner(selectedPulse);
                            }
                        });
            }

            @Override
            public void failure(Throwable t, int errorMessage) {
                try {
                    Snackbar snackbar = Snackbar.make(mContentLayout, errorMessage,
                            Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.alert(snackbar).show();
                } catch (IllegalStateException ignored) {
                }
            }
        });
    }

    @Override
    protected String getTrackedViewName() {
        if (mViewPager == null || mViewPagerAdapter.getSelectedPulseTarget() == null) {
            return "Pulse";
        }

        final String[] pagesNames = {"EventStats", "AtendeeStats", "CircleStats"};
        String pageName;
        try {
            pageName = pagesNames[getCurrentPage()];
        } catch (IndexOutOfBoundsException e) {
            pageName = "";
        }

        return "Pulse/" + mViewPagerAdapter.getSelectedPulseTarget().replaceAll(" ", "-")
                + "/" + pageName;
    }

    private void initSpinner(String selectedPulse) {
        Collections.sort(mPulseTargets);
        mPulseTargets.add(0, PulseFragment.GLOBAL);

        Toolbar toolbar = getActionBarToolbar();
        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.actionbar_spinner,
                toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        mSpinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);

        mSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_actionbar, mPulseTargets);
        mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                String previous = mViewPagerAdapter.getSelectedPulseTarget();
                if (!previous.equals(mSpinnerAdapter.getItem(position))) {
                    refreshSpinner(mSpinnerAdapter.getItem(position));
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // Nothing to do.
            }
        });

        refreshSpinner(selectedPulse);
    }

    private void refreshSpinner(String selectedPulse) {

        if (selectedPulse == null) {
            selectedPulse = mPulseTargets.get(0);
        }
        mSpinner.setSelection(mPulseTargets.indexOf(selectedPulse));

        mViewPagerAdapter = new PulsePagerAdapter(this, getSupportFragmentManager());
        mViewPagerAdapter.setSelectedPulseTarget(selectedPulse);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private ArrayList<String> getPulseTargets() {
        return mPulseTargets;
    }

    @Override
    public void openPulse(final String key) {
        mSpinner.setSelection(getPulseTargets().indexOf(key));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSpinner != null) {
            outState.putString(INSTANCE_STATE_SELECTED_PULSE, (String) mSpinner.getSelectedItem());
        }
    }

    public class PulsePagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private String mSelectedPulseTarget;

        public PulsePagerAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mSelectedPulseTarget == null ? 0 : 3;
        }

        @Override
        public Fragment getItem(int position) {
            return PulseFragment.newInstance(position, mSelectedPulseTarget);
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
            if (mSelectedPulseTarget != null) {
                trackView();
            }

            mSelectedPulseTarget = pulseTarget;
        }
    }
}
