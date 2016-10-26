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

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;

public class PulseActivity extends GdgNavDrawerActivity implements PulseFragment.Callbacks {

    private static final String INSTANCE_STATE_SELECTED_PULSE = "INSTANCE_STATE_SELECTED_PULSE";
    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    ArrayAdapter<String> mSpinnerAdapter;
    PulsePagerAdapter mViewPagerAdapter;
    ArrayList<String> mPulseTargets = new ArrayList<>();
    Spinner mSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pulse);

        final String selectedPulse;
        if (savedInstanceState != null) {
            selectedPulse = savedInstanceState.getString(INSTANCE_STATE_SELECTED_PULSE);
        } else {
            selectedPulse = PulseFragment.GLOBAL;
        }

        App.getInstance().getModelCache().getAsync(ModelCache.KEY_PULSE_GLOBAL, true, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Pulse pulse = (Pulse) item;
                setupPulseScreen(pulse, selectedPulse);
            }

            @Override
            public void onNotFound(String key) {
                fetchPulse(selectedPulse);
            }
        });
    }

    void fetchPulse(final String selectedPulse) {
        App.getInstance().getGroupDirectory().getPulse().enqueue(new Callback<Pulse>() {
            @Override
            public void success(final Pulse pulse) {
                App.getInstance().getModelCache().putAsync(
                    ModelCache.KEY_PULSE_GLOBAL,
                    pulse,
                    DateTime.now().plusDays(1),
                    new ModelCache.CachePutListener() {
                        @Override
                        public void onPutIntoCache() {
                            setupPulseScreen(pulse, selectedPulse);
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

    void setupPulseScreen(Pulse pulse, String selectedPulse) {
        mPulseTargets.addAll(pulse.keySet());
        Collections.sort(mPulseTargets);
        mPulseTargets.add(0, PulseFragment.GLOBAL);

        initSpinner();
        openPulse(selectedPulse);
    }

    private void initSpinner() {

        Toolbar toolbar = getActionBarToolbar();
        View spinnerContainer = LayoutInflater.from(this).inflate(R.layout.actionbar_spinner, toolbar, false);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        toolbar.addView(spinnerContainer, lp);

        mSpinner = (Spinner) spinnerContainer.findViewById(R.id.actionbar_spinner);

        mSpinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item_actionbar, mPulseTargets);
        mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view,
                                       final int position, final long id) {
                String previous = getSelectedPulseTarget();
                String selected = mSpinnerAdapter.getItem(position);
                if (previous == null || !previous.equals(selected)) {
                    onPulseItemSelected(selected);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
                // Nothing to do.
            }
        });
    }

    void onPulseItemSelected(String selectedPulse) {
        mViewPagerAdapter = new PulsePagerAdapter(getResources(), getSupportFragmentManager(), selectedPulse);
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void openPulse(final String key) {
        mSpinner.setSelection(mPulseTargets.indexOf(key));
    }

    @Override
    protected String getTrackedViewName() {
        if (mViewPager == null || getSelectedPulseTarget() == null) {
            return "Pulse";
        }
        final String[] pagesNames = {"EventStats", "AtendeeStats", "CircleStats"};
        String pageName;
        try {
            pageName = pagesNames[getCurrentPage()];
        } catch (IndexOutOfBoundsException e) {
            pageName = "";
        }

        return "Pulse/" + getSelectedPulseTarget().replaceAll(" ", "-")
            + "/" + pageName;
    }

    @Override
    public void onBackPressed() {
        if (mSpinner != null && !PulseFragment.GLOBAL.equals(mSpinner.getSelectedItem())) {
            openPulse(PulseFragment.GLOBAL);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSpinner != null) {
            outState.putString(INSTANCE_STATE_SELECTED_PULSE, (String) mSpinner.getSelectedItem());
        }
    }

    @Nullable
    String getSelectedPulseTarget() {
        return mViewPagerAdapter != null
            ? mViewPagerAdapter.getSelectedPulseTarget() : null;
    }

    public static class PulsePagerAdapter extends FragmentPagerAdapter {
        private final Resources resources;
        private final String selectedPulseTarget;

        PulsePagerAdapter(Resources resources, FragmentManager fm, String selectedPulseTarget) {
            super(fm);
            this.resources = resources;
            this.selectedPulseTarget = selectedPulseTarget;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            return PulseFragment.newInstance(position, selectedPulseTarget);
        }

        @Override
        public long getItemId(int position) {
            return selectedPulseTarget.hashCode() * 10 + position;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return resources.getText(R.string.pulse_events);
                case 1:
                    return resources.getText(R.string.pulse_attendees);
                case 2:
                    return resources.getText(R.string.pulse_circlers);
                default:
                    throw new IllegalStateException("The size of the adapter should be 3");
            }
        }

        public String getSelectedPulseTarget() {
            return selectedPulseTarget;
        }
    }
}
