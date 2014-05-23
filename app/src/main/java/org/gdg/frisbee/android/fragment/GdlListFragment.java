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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.PlusClient;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.activity.GdlActivity;
import org.gdg.frisbee.android.activity.YoutubeActivity;
import org.gdg.frisbee.android.adapter.GdlAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GoogleDevelopersLive;
import org.gdg.frisbee.android.api.model.GdlShow;
import org.gdg.frisbee.android.api.model.GdlShowList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;
import timber.log.Timber;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public class GdlListFragment extends GdgListFragment implements GdlActivity.Listener { // TODO: implements PullToRefreshAttacher.OnRefreshListener

    private static final String LOG_TAG = "GDG-GdlListFragment";
    private GoogleDevelopersLive mClient;

    @InjectView(android.R.id.list)
    GridView mGrid;

    private GdlAdapter mAdapter;

    public static GdlListFragment newInstance(String cat, boolean active) {
        GdlListFragment fragment = new GdlListFragment();
        Bundle arguments = new Bundle();
        arguments.putString("category", cat);
        arguments.putBoolean("active", active);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.d("onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");

        for(int i = 0; i <= mList.getChildCount(); i++) {
            mAdapter.updatePlusOne(mList.getChildAt(i));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated()");

        mClient = new GoogleDevelopersLive();

        mAdapter = new GdlAdapter(getActivity(), ((GdgActivity)getActivity()).getGoogleApiClient());
        setListAdapter(mAdapter);

        setIsLoading(true);

        fetchContent(false);

        if(getArguments().getBoolean("active")) {
            onPageSelected();
        }
    }

    private void setContent(final GdlShowList gdlShows, final boolean wasForced) {
        App.getInstance().getModelCache().putAsync("gdl_shows_"+ getArguments().get("category"), gdlShows, DateTime.now().plusHours(3), new ModelCache.CachePutListener() {
            @Override
            public void onPutIntoCache() {
                mAdapter.clear();
                mAdapter.addAll(gdlShows);
                setIsLoading(false);

                /*if(wasForced)
                    ((GdgActivity)getActivity()).getPullToRefreshHelper().setRefreshComplete();*/
            }
        });
    }

    private void fetchContent(final boolean force) {
        setIsLoading(true);
        final ApiRequest mRequest = mClient.getRecordedShows(getArguments().getString("category"),
                new Response.Listener<GdlShowList>() {
                    @Override
                    public void onResponse(final GdlShowList gdlShows) {
                        setContent(gdlShows, force);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if(getActivity() != null &&isAdded())
                            Crouton.makeText(getActivity(), "Fetch failed", Style.ALERT).show();
                    }
                });

        App.getInstance().getModelCache().getAsync("gdl_shows_"+ getArguments().get("category"), true, force, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                if(!force) {
                    Timber.d("from Cache.");
                }

                setContent((GdlShowList) item, force);
            }

            @Override
            public void onNotFound(String key) {
                mRequest.execute();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("onCreateView()");
        View v = inflater.inflate(R.layout.fragment_gdl_list, null);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final GdlShow show = (GdlShow) getListView().getItemAtPosition(position);
        Intent playVideoIntent = new Intent(getActivity(), YoutubeActivity.class);
        playVideoIntent.putExtra("gdl", true);
        playVideoIntent.putExtra("video_id", show.getYoutubeId());
        startActivity(playVideoIntent);
    }

    /*@Override
    public void onRefreshStarted(View view) {
        if(Utils.isOnline(getActivity())) {
            fetchContent(true);
        } else {
            if(isAdded())
                Crouton.makeText(getActivity(), getString(R.string.offline_alert), Style.ALERT).show();
        }
    }*/

    @Override
    public void onPageSelected() {
        //((GdgActivity)getActivity()).getPullToRefreshHelper().addRefreshableView(getListView(), this);
    }
}
