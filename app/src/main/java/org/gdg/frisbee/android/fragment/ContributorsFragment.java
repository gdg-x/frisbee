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

package org.gdg.frisbee.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.adapter.ContributorAdapter;
import org.gdg.frisbee.android.adapter.GdlAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GitHub;
import org.gdg.frisbee.android.api.GoogleDevelopersLive;
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.api.model.GdlShowList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;
import timber.log.Timber;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 08.07.13
 * Time: 01:49
 * To change this template use File | Settings | File Templates.
 */
public class ContributorsFragment extends GdgListFragment {

    private static final String LOG_TAG = "GDG-ContributorsFragment";
    private LayoutInflater mInflater;
    private GitHub mClient;
    private ApiRequest mFetchContent;
    private ContributorAdapter mAdapter;

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

        mClient = new GitHub();
        mAdapter = new ContributorAdapter(getActivity(), 0);
        setListAdapter(mAdapter);

        mFetchContent = mClient.getContributors(Const.GITHUB_ORGA, Const.GITHUB_REPO,
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
                        //To change body of implemented methods use File | Settings | File Templates.
                    }
                });

        if(Utils.isOnline(getActivity())) {
            mFetchContent.execute();
        } else {
            App.getInstance().getModelCache().getAsync("frisbee_contributors", false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ArrayList<Contributor> contributors = (ArrayList<Contributor>)item;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("onCreateView()");
        View v = inflater.inflate(R.layout.fragment_gdl_list, null);
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
