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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ContributorAdapter;
import org.gdg.frisbee.android.api.GitHub;
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.api.model.ContributorList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.converter.GsonConverter;
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
        if (Utils.isOnline(getActivity())) {
            fetchGitHubContributors();
        } else {
            App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_FRISBEE_CONTRIBUTORS, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ContributorList contributors = (ContributorList) item;

                    Crouton.makeText(getActivity(), R.string.cached_content,
                            Style.INFO, R.id.content_frame).show();
                    mAdapter.addAll(contributors);
                }

                @Override
                public void onNotFound(String key) {
                    Crouton.makeText(getActivity(), R.string.offline_alert,
                            Style.ALERT, R.id.content_frame).show();
                }
            });
        }
    }
    
    private void fetchGitHubContributors() {
        GitHub gitHubClient = new RestAdapter.Builder()
                .setEndpoint("https://api.github.com")
                .setConverter(new GsonConverter(Utils.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)))
                .build().create(GitHub.class);
        
        gitHubClient.getContributors(Const.GITHUB_ORGA, Const.GITHUB_REPO, new Callback<ContributorList>() {
            @Override
            public void success(final ContributorList contributors, retrofit.client.Response response) {
                App.getInstance().getModelCache().putAsync(Const.CACHE_KEY_FRISBEE_CONTRIBUTORS,
                        contributors,
                        DateTime.now().plusDays(1),
                        new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                mAdapter.addAll(contributors);
                            }
                        });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gde_list, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Contributor contributor = mAdapter.getItem(position);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(contributor.getHtmlUrl()));
        startActivity(i);
    }
}
