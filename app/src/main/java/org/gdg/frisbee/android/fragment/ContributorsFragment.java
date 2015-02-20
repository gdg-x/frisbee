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

package org.gdg.frisbee.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ContributorAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GitHub;
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.util.ArrayList;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

public class ContributorsFragment extends GdgListFragment {

    protected ContributorAdapter mAdapter;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.d("onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated()");

        mAdapter = new ContributorAdapter(getActivity());
        setListAdapter(mAdapter);

        loadContributors();
    }

    protected void loadContributors() {
        GitHub githubClient = new GitHub();
        final ApiRequest mFetchContent = githubClient.getContributors(Const.GITHUB_ORGA, Const.GITHUB_REPO,
                new Response.Listener<ArrayList<Contributor>>() {
                    @Override
                    public void onResponse(final ArrayList<Contributor> contributors) {
                        App.getInstance().getModelCache().putAsync("frisbee_contributors", contributors, DateTime.now().plusDays(1), new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                mAdapter.addAll(contributors);
                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                });

        if (Utils.isOnline(getActivity())) {
            mFetchContent.execute();
        } else {
            App.getInstance().getModelCache().getAsync("frisbee_contributors", false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ArrayList<Contributor> contributors = (ArrayList<Contributor>) item;

                    Crouton.makeText(getActivity(), getString(R.string.cached_content), Style.INFO).show();
                    mAdapter.addAll(contributors);
                }

                @Override
                public void onNotFound(String key) {
                    Crouton.makeText(getActivity(), getString(R.string.offline_alert), Style.ALERT).show();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("onCreateView()");
        View v = inflater.inflate(R.layout.fragment_gde_list, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart()");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Contributor contributor = mAdapter.getItem(position);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(contributor.getHtmlUrl()));
        startActivity(i);
    }
}
