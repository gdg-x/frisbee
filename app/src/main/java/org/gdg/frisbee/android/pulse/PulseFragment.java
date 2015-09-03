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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.api.model.PulseEntry;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgListFragment;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.Map;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import timber.log.Timber;

public class PulseFragment extends GdgListFragment {

    public static final String GLOBAL = "Global";

    private static final String ARG_MODE = "mode";
    private static final String ARG_TARGET = "target";

    private int mMode;
    private String mTarget;
    private PulseAdapter mAdapter;
    private Callbacks mListener;

    public static PulseFragment newInstance(int mode, String target) {
        PulseFragment fragment = new PulseFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARG_MODE, mode);
        arguments.putString(ARG_TARGET, target);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callbacks) {
            mListener = (Callbacks) activity;
        } else {
            throw new ClassCastException("Activity " + activity.getClass().getSimpleName()
                    + " must implement " + Pulse.class.getSimpleName() + " interface.");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTarget = getArguments().getString(ARG_TARGET);
        mMode = getArguments().getInt(ARG_MODE);

        mAdapter = new PulseAdapter(getActivity());
        setListAdapter(mAdapter);

        setIsLoading(true);

        App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_PULSE + mTarget.toLowerCase().replace(" ", "-"),
                true,
                new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Pulse pulse = (Pulse) item;
                        initAdapter(pulse);
                    }

                    @Override
                    public void onNotFound(String key) {
                        fetchPulseTask();
                    }
                });
    }

    private void fetchPulseTask() {
        if (mTarget.equals(GLOBAL)) {
            App.getInstance().getGroupDirectory().getPulse(new Callback<Pulse>() {
                @Override
                public void success(final Pulse pulse, retrofit.client.Response response) {
                    App.getInstance().getModelCache().putAsync(
                            Const.CACHE_KEY_PULSE + mTarget.toLowerCase(),
                            pulse,
                            DateTime.now().plusDays(1),
                            new ModelCache.CachePutListener() {
                                @Override
                                public void onPutIntoCache() {
                                    initAdapter(pulse);
                                }
                            });
                }

                @Override
                public void failure(RetrofitError error) {
                    if (isAdded()) {
                        Snackbar snackbar = Snackbar.make(getView(), R.string.fetch_chapters_failed,
                                Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.alert(snackbar).show();
                    }
                    Timber.e(error, "Couldn't fetch pulse");
                }
            });
        } else {
            App.getInstance().getGroupDirectory().getCountryPulse(mTarget, new Callback<Pulse>() {
                @Override
                public void success(final Pulse pulse, retrofit.client.Response response) {
                    App.getInstance().getModelCache().putAsync(
                            Const.CACHE_KEY_PULSE + mTarget.toLowerCase().replace(" ", "-"),
                            pulse,
                            DateTime.now().plusDays(1),
                            new ModelCache.CachePutListener() {
                                @Override
                                public void onPutIntoCache() {
                                    initAdapter(pulse);
                                }
                            });
                }

                @Override
                public void failure(RetrofitError error) {
                    if (isAdded()) {
                        Snackbar snackbar = Snackbar.make(getView(), R.string.fetch_chapters_failed,
                                Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.alert(snackbar).show();
                    }
                    Timber.e(error, "Couldn't fetch pulse");
                }
            });
        }
    }

    private void initAdapter(Pulse pulse) {
        mAdapter.setPulse(mMode, pulse);
        mAdapter.notifyDataSetChanged();
        setIsLoading(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pulse, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Map.Entry<String, PulseEntry> pulse = mAdapter.getItem(position);

        if (mTarget.equals(GLOBAL)) {
            mListener.openPulse(pulse.getKey());
        } else {
            Intent chapterIntent = new Intent(getActivity(), MainActivity.class);
            chapterIntent.putExtra(Const.EXTRA_CHAPTER_ID, pulse.getValue().getId());
            startActivity(chapterIntent);
        }
    }

    public interface Callbacks {
        void openPulse(String key);
    }
}
