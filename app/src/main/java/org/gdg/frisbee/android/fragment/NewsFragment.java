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
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusShare;
import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.api.GapiTransportChooser;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.adapter.NewsAdapter;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.io.IOException;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 20.04.13
 * Time: 12:22
 */
public class NewsFragment extends GdgListFragment implements PullToRefreshAttacher.OnRefreshListener {

    private static final String LOG_TAG = "GDG-NewsFragment";

    final HttpTransport mTransport = GapiTransportChooser.newCompatibleTransport();
    final JsonFactory mJsonFactory = new GsonFactory();

    private Plus mClient;

    private NewsAdapter mAdapter;

    public static NewsFragment newInstance(String plusId) {
        NewsFragment fragment = new NewsFragment();
        Bundle arguments = new Bundle();
        arguments.putString("plus_id", plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");

        for(int i = 0; i <= getListView().getChildCount(); i++) {
            mAdapter.updatePlusOne(getListView().getChildAt(i));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated()");

        mClient = new Plus.Builder(mTransport, mJsonFactory, null).setGoogleClientRequestInitializer(new CommonGoogleJsonClientRequestInitializer(getString(R.string.ip_simple_api_access_key))).build();

        PlusClient plusClient = null;
        if(((GdgActivity)getActivity()).getPlayServicesHelper() != null) {
            plusClient = ((GdgActivity)getActivity()).getPlayServicesHelper().getPlusClient();
        }
        mAdapter = new NewsAdapter(getActivity(), plusClient);
        setListAdapter(mAdapter);

        registerForContextMenu(getListView());

        ((GdgActivity)getActivity()).getPullToRefreshHelper().addRefreshableView(getListView(), new PullToRefreshAttacher.ViewDelegate() {
            @Override
            public boolean isScrolledToTop(View view) {
                AbsListView absListView = (AbsListView) view;
                if (absListView.getCount() == 0) {
                    return true;
                } else if (absListView.getFirstVisiblePosition() == 0) {
                    final View firstVisibleChild = absListView.getChildAt(0);
                    return firstVisibleChild != null && firstVisibleChild.getTop() >= 0;
                }
                return false;
            }
        }, this);

        if(getListView() instanceof ListView) {
            ListView listView = (ListView) getListView();
            listView.setDivider(null);
            listView.setDividerHeight(0);
        }

        if(Utils.isOnline(getActivity())) {
            new Builder<String, ActivityFeed>(String.class, ActivityFeed.class)
                    .addParameter(getArguments().getString("plus_id"))
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

                                ActivityFeed feed = (ActivityFeed) App.getInstance().getModelCache().get("news_" + params[0]);

                                if (feed == null) {
                                    Plus.Activities.List request = mClient.activities().list(params[0], "public");
                                    request.setMaxResults(10L);
                                    request.setFields("nextPageToken,items(id,published,url,object/content,verb,object/attachments,object/actor,annotation,object(plusoners,replies,resharers))");
                                    feed = request.execute();

                                    App.getInstance().getModelCache().put("news_" + params[0], feed, DateTime.now().plusHours(1));
                                }

                                return feed;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    })
                    .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<ActivityFeed>() {
                        @Override
                        public void onPostExecute(ActivityFeed activityFeed) {
                            if(activityFeed != null) {
                                mAdapter.addAll(activityFeed.getItems());
                                setIsLoading(false);
                            }
                        }
                    })
                    .buildAndExecute();
        } else {
            App.getInstance().getModelCache().getAsync("news_" +getArguments().getString("plus_id"), false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ActivityFeed feed = (ActivityFeed)item;

                    if(isAdded())
                        Crouton.makeText(getActivity(), getString(R.string.cached_content), Style.INFO).show();

                    mAdapter.addAll(feed.getItems());
                    setIsLoading(false);
                }

                @Override
                public void onNotFound(String key) {
                    if(isAdded())
                        Crouton.makeText(getActivity(), getString(R.string.offline_alert), Style.ALERT).show();
                }
            });
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        getActivity().getMenuInflater().inflate(R.menu.news_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Activity activity = (Activity) mAdapter.getItem(info.position);

        switch(item.getItemId()) {
            case R.id.share_with_googleplus:
                shareWithGooglePlus(activity);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void shareWithGooglePlus(Activity activity) {
        Intent shareIntent = new PlusShare.Builder(getActivity())
                .setType("text/plain")
                .setContentUrl(Uri.parse(activity.getUrl()))
                .getIntent();

        startActivityForResult(shareIntent, 0);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView()");
        return inflater.inflate(R.layout.fragment_news, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
    }

    @Override
    public void onRefreshStarted(View view) {
        if(Utils.isOnline(getActivity())) {
            new Builder<String, ActivityFeed>(String.class, ActivityFeed.class)
                    .addParameter(getArguments().getString("plus_id"))
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

                                    Plus.Activities.List request = mClient.activities().list(params[0], "public");
                                    request.setMaxResults(10L);
                                    request.setFields("nextPageToken,items(id,published,url,object/content,verb,object/attachments,annotation,object(plusoners,replies,resharers))");
                                    ActivityFeed feed = request.execute();

                                    App.getInstance().getModelCache().put("news_" + params[0], feed, DateTime.now().plusHours(1));

                                return feed;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    })
                    .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<ActivityFeed>() {
                        @Override
                        public void onPostExecute(ActivityFeed activityFeed) {
                            if(activityFeed != null) {
                                mAdapter.replaceAll(activityFeed.getItems(), 0);
                                setIsLoading(false);

                                if(getActivity() != null)
                                    ((GdgActivity)getActivity()).getPullToRefreshHelper().setRefreshComplete();
                            }
                        }
                    })
                    .buildAndExecute();
        }
    }
}
