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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.Bind;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class PulseActivity extends GdgNavDrawerActivity implements PulseFragment.Callbacks {

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
        mSpinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_actionbar);
        mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_PULSE_GLOBAL, true, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Pulse pulse = (Pulse) item;
                mPulseTargets.addAll(pulse.keySet());
                initSpinner();
            }

            @Override
            public void onNotFound(String key) {
                fetchPulse();
            }
        });
    }

    private void fetchPulse() {
        App.getInstance().getGroupDirectory().getPulse(new Callback<Pulse>() {
            @Override
            public void success(@NonNull final Pulse pulse, Response response) {
                App.getInstance().getModelCache().putAsync(
                        Const.CACHE_KEY_PULSE_GLOBAL,
                        pulse,
                        DateTime.now().plusDays(1),
                        new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                mPulseTargets.addAll(pulse.keySet());
                                initSpinner();
                            }
                        });
            }

            @Override
            public void failure(RetrofitError error) {
                try {
                    Snackbar snackbar = Snackbar.make(mContentLayout, R.string.fetch_chapters_failed,
                            Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.alert(snackbar).show();
                } catch (IllegalStateException exception) {
                }
                Timber.e(error, "Couldn't fetch chapter list");
            }
        });
    }

    @NonNull
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
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public ArrayList<String> getPulseTargets() {
        return mPulseTargets;
    }

    @Override
    public void openPulse(final String key) {
        mSpinner.setSelection(getPulseTargets().indexOf(key));
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

        @Nullable
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
            if (mSelectedPulseTarget != null) {
                trackView();
            }

            mSelectedPulseTarget = pulseTarget;
        }
    }
}
