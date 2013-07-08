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
import android.widget.ListView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
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

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 21:32
 * To change this template use File | Settings | File Templates.
 */
public class GdlListFragment extends GdgListFragment {

    private static final String LOG_TAG = "GDG-GdlListFragment";
    private GoogleDevelopersLive mClient;
    private ApiRequest mFetchContent;

    private GdlAdapter mAdapter;

    public static GdlListFragment newInstance(String cat) {
        GdlListFragment fragment = new GdlListFragment();
        Bundle arguments = new Bundle();
        arguments.putString("category", cat);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");

        for(int i = 0; i <= mList.getChildCount(); i++) {
            mAdapter.updatePlusOne(mList.getChildAt(i));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated()");

        mClient = new GoogleDevelopersLive();
        mAdapter = new GdlAdapter(getActivity(), ((GdgActivity)getActivity()).getPlayServicesHelper().getPlusClient());
        setListAdapter(mAdapter);

        setIsLoading(true);

        mFetchContent = mClient.getRecordedShows(getArguments().getString("category"),
                new Response.Listener<GdlShowList>() {
                    @Override
                    public void onResponse(final GdlShowList gdlShows) {
                        App.getInstance().getModelCache().putAsync("gdl_shows_"+ getArguments().get("category"), gdlShows, DateTime.now().plusHours(3), new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                mAdapter.addAll(gdlShows);
                                setIsLoading(false);
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
            App.getInstance().getModelCache().getAsync("gdl_shows_"+ getArguments().get("category"), false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    GdlShowList feed = (GdlShowList)item;

                    Crouton.makeText(getActivity(), getString(R.string.cached_content), Style.INFO).show();
                    mAdapter.addAll(feed);
                    setIsLoading(false);
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
        Log.d(LOG_TAG, "onCreateView()");
        return inflater.inflate(R.layout.fragment_gdl_list, null);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

    }
}
