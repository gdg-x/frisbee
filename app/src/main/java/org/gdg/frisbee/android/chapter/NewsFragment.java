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

package org.gdg.frisbee.android.chapter;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.ActivityFeed;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GapiOkTransport;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.fragment.SwipeRefreshRecyclerViewFragment;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.io.IOException;

import butterknife.ButterKnife;

public class NewsFragment extends SwipeRefreshRecyclerViewFragment
        implements SwipeRefreshLayout.OnRefreshListener {

    final HttpTransport mTransport = new GapiOkTransport();
    final JsonFactory mJsonFactory = new GsonFactory();

    private Plus mClient;

    private NewsAdapter mAdapter;

    public static NewsFragment newInstance(String plusId) {
        NewsFragment fragment = new NewsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_PLUS_ID, plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        for (int i = 0; i <= getListView().getChildCount(); i++) {
            mAdapter.updatePlusOne(getListView().getChildAt(i));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mClient = new Plus.Builder(mTransport, mJsonFactory, null)
                .setGoogleClientRequestInitializer(
                        new CommonGoogleJsonClientRequestInitializer(BuildConfig.IP_SIMPLE_API_ACCESS_KEY))
                .build();

        StaggeredGridLayoutManager layoutManager = 
                new StaggeredGridLayoutManager(
                        getResources().getInteger(R.integer.news_fragment_column_count), 
                        StaggeredGridLayoutManager.VERTICAL);
        getListView().setLayoutManager(layoutManager);
        
        mAdapter = new NewsAdapter(getActivity(), ((GdgActivity) getActivity()).getGoogleApiClient());
        setRecyclerAdapter(mAdapter);

        setOnRefreshListener(this);

        final String plusId = getArguments().getString(Const.EXTRA_PLUS_ID);
        if (Utils.isOnline(getActivity())) {
            new Builder<>(String.class, ActivityFeed.class)
                    .addParameter(plusId)
                    .setOnPreExecuteListener(new CommonAsyncTask.OnPreExecuteListener() {
                        @Override
                        public void onPreExecute() {
                            setIsLoading(true);
                        }
                    })
                    .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, ActivityFeed>() {
                        @Override
                        public ActivityFeed doInBackground(String... params) {
                            try {

                                ActivityFeed feed = (ActivityFeed) App.getInstance().getModelCache().get(Const.CACHE_KEY_NEWS + params[0]);

                                if (feed == null) {
                                    Plus.Activities.List request = mClient.activities().list(params[0], "public");
                                    request.setMaxResults(10L);
                                    request.setFields(
                                            "nextPageToken,"
                                                    + "items(id,published,url,object/content,verb,"
                                                    + "object/attachments,object/actor,annotation,object(plusoners,replies,resharers))");
                                    feed = request.execute();

                                    App.getInstance().getModelCache().put(Const.CACHE_KEY_NEWS + params[0], feed, DateTime.now().plusHours(1));
                                }

                                return feed;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    })
                    .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<String, ActivityFeed>() {
                        @Override
                        public void onPostExecute(String[] params, ActivityFeed activityFeed) {
                            if (activityFeed != null) {
                                mAdapter.addAll(activityFeed.getItems());
                                setIsLoading(false);
                            }
                        }
                    })
                    .buildAndExecute();
        } else {
            App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_NEWS + plusId, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ActivityFeed feed = (ActivityFeed) item;

                    if (isAdded()) {
                        Snackbar snackbar = Snackbar.make(getView(), R.string.cached_content,
                                Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.info(snackbar).show();
                    }

                    mAdapter.addAll(feed.getItems());
                    setIsLoading(false);
                }

                @Override
                public void onNotFound(String key) {
                    if (isAdded()) {
                        Snackbar snackbar = Snackbar.make(getView(), R.string.offline_alert,
                                Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.alert(snackbar).show();
                    }
                }
            });
        }
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        getActivity().getMenuInflater().inflate(R.menu.news_context, menu);
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        NewsAdapter.Item selectedItem = mAdapter.getItem(info.position);
//
//        switch (item.getItemId()) {
//            case R.id.share_with_googleplus:
//                if (selectedItem != null) {
//                    shareWithGooglePlus(selectedItem.getActivity());
//                }
//                return true;
//            default:
//        return super.onContextItemSelected(item);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, v);
        return createSwipeRefresh(v);
    }

    @Override
    public void onRefresh() {
        if (Utils.isOnline(getActivity())) {
            new Builder<>(String.class, ActivityFeed.class)
                    .addParameter(getArguments().getString(Const.EXTRA_PLUS_ID))
                    .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, ActivityFeed>() {
                        @Override
                        public ActivityFeed doInBackground(String... params) {
                            try {

                                Plus.Activities.List request = mClient.activities().list(params[0], "public");
                                request.setMaxResults(10L);
                                request.setFields(
                                        "nextPageToken,"
                                                + "items(id,published,url,object/content,verb,"
                                                + "object/attachments,annotation,object(plusoners,replies,resharers))");
                                ActivityFeed feed = request.execute();

                                App.getInstance().getModelCache().put(Const.CACHE_KEY_NEWS + params[0], feed, DateTime.now().plusHours(1));

                                return feed;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    })
                    .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<String, ActivityFeed>() {
                        @Override
                        public void onPostExecute(String[] params, ActivityFeed activityFeed) {
                            if (activityFeed != null) {
                                mAdapter.replaceAll(activityFeed.getItems(), 0);
                                mAdapter.notifyDataSetChanged();

                                if (getActivity() != null) {
                                    setRefreshing(false);
                                }
                            }
                        }
                    })
                    .buildAndExecute();
        }
    }
}
