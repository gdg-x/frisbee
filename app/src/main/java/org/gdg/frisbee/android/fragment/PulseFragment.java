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

package org.gdg.frisbee.android.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.MainActivity;
import org.gdg.frisbee.android.adapter.PulseAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.PulseEntry;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.joda.time.DateTime;

import java.util.Map;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

public class PulseFragment extends GdgListFragment {

    private static final String ARG_MODE = "mode";
    private static final String ARG_TARGET = "target";

    private int mMode;
    private String mTarget;
    private PulseAdapter mAdapter;
    private ApiRequest mFetchPulseTask;
    private Pulse mListener;

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
        if (activity instanceof Pulse) {
            mListener = (Pulse) activity;
        } else {
            throw new ClassCastException("Activity " + activity.getClass().getSimpleName()
                    + " must implement " + Pulse.class.getSimpleName() + " interface.");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final GroupDirectory client = new GroupDirectory();

        mTarget = getArguments().getString(ARG_TARGET);
        mMode = getArguments().getInt(ARG_MODE);

        if (getListView() instanceof ListView) {
            ListView listView = (ListView) getListView();
            listView.setDivider(null);
            listView.setDividerHeight(0);
        }

        mAdapter = new PulseAdapter(getActivity());
        setListAdapter(mAdapter);

        setIsLoading(true);

        if (mTarget.equals("Global")) {
            mFetchPulseTask = client.getPulse(
                    new Response.Listener<org.gdg.frisbee.android.api.model.Pulse>() {
                        @Override
                        public void onResponse(final org.gdg.frisbee.android.api.model.Pulse pulse) {
                            App.getInstance().getModelCache().putAsync(
                                    "pulse_" + mTarget.toLowerCase(),
                                    pulse,
                                    DateTime.now().plusDays(1),
                                    new ModelCache.CachePutListener() {
                                        @Override
                                        public void onPutIntoCache() {
                                            initAdapter(pulse);
                                        }
                                    });
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            if (isAdded()) {
                                Crouton.makeText(getActivity(), getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                            }
                            Timber.e("Couldn't fetch pulse", volleyError);
                        }
                    }
            );
        } else {
            mFetchPulseTask = client.getCountryPulse(mTarget,
                    new Response.Listener<org.gdg.frisbee.android.api.model.Pulse>() {
                        @Override
                        public void onResponse(final org.gdg.frisbee.android.api.model.Pulse pulse) {
                            App.getInstance().getModelCache().putAsync(
                                    "pulse_" + mTarget.toLowerCase().replace(" ", "-"),
                                    pulse,
                                    DateTime.now().plusDays(1),
                                    new ModelCache.CachePutListener() {
                                        @Override
                                        public void onPutIntoCache() {
                                            initAdapter(pulse);
                                        }
                                    });
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            if (isAdded()) {
                                Crouton.makeText(getActivity(), getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                            }
                            Timber.e("Couldn't fetch pulse", volleyError);
                        }
                    }
            );
        }

        App.getInstance().getModelCache().getAsync("pulse_" + mTarget.toLowerCase().replace(" ", "-"), true, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                org.gdg.frisbee.android.api.model.Pulse pulse = (org.gdg.frisbee.android.api.model.Pulse) item;
                initAdapter(pulse);
            }

            @Override
            public void onNotFound(String key) {
                mFetchPulseTask.execute();
            }
        });
    }

    private void initAdapter(org.gdg.frisbee.android.api.model.Pulse pulse) {
        mAdapter.setPulse(mMode, pulse);
        mAdapter.notifyDataSetChanged();
        setIsLoading(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pulse, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Map.Entry<String, PulseEntry> pulse = mAdapter.getItem(position);

        if (mTarget.equals("Global")) {
            mListener.openPulse(pulse.getKey());
        } else {
            Intent chapterIntent = new Intent(getActivity(), MainActivity.class);
            chapterIntent.putExtra(Const.EXTRA_CHAPTER_ID, pulse.getValue().getId());
            startActivity(chapterIntent);
        }
    }

    public interface Pulse {
        void openPulse(String key);
    }
}
