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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.plus.Activities;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.fragment.SwipeRefreshRecyclerViewFragment;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

public class NewsFragment extends SwipeRefreshRecyclerViewFragment
    implements SwipeRefreshLayout.OnRefreshListener {

    private static final String CACHE_KEY_NEWS = "news2_";

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

        RecyclerView recyclerView = getListView();
        for (int i = 0, size = recyclerView.getChildCount(); i <= size; i++) {
            View child = recyclerView.getChildAt(i);
            if (child != null) {
                mAdapter.updatePlusOne((NewsAdapter.ViewHolder)
                    recyclerView.getChildViewHolder(child));
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
            setIsLoading(true);
            App.getInstance().getModelCache()
                .getAsync(CACHE_KEY_NEWS + plusId, new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Activities activityFeed = (Activities) item;
                        mAdapter.addAll(activityFeed.getItems());
                        setIsLoading(false);
                    }

                    @Override
                    public void onNotFound(String key) {
                        App.getInstance().getPlusApi().getActivities(plusId).enqueue(
                            new Callback<Activities>() {
                                @Override
                                public void success(Activities activityFeed) {
                                    if (activityFeed != null) {
                                        mAdapter.addAll(activityFeed.getItems());
                                    } else {
                                        // TODO show empty view
                                    }
                                    setIsLoading(false);
                                }
                            });
                    }
                });
        } else {
            App.getInstance().getModelCache().getAsync(CACHE_KEY_NEWS + plusId,
                false,
                new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Activities feed = (Activities) item;

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
                        showError(R.string.offline_alert);
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
        View v = inflateView(inflater, R.layout.fragment_news, container);
        return createSwipeRefresh(v);
    }

    @Override
    public void onRefresh() {
        if (Utils.isOnline(getActivity())) {
            final String plusId = getArguments().getString(Const.EXTRA_PLUS_ID);
            App.getInstance().getPlusApi().getActivities(plusId).enqueue(
                new Callback<Activities>() {
                    @Override
                    public void success(Activities activityFeed) {
                        if (activityFeed != null) {
                            cacheActivityFeed(plusId, activityFeed);
                            mAdapter.replaceAll(activityFeed.getItems(), 0);
                            mAdapter.notifyDataSetChanged();

                            if (getActivity() != null) {
                                setRefreshing(false);
                            }
                        }
                    }
                });
        }
    }

    public void cacheActivityFeed(String plusId, Activities feed) {
        App.getInstance().getModelCache().putAsync(CACHE_KEY_NEWS + plusId, feed,
            DateTime.now().plusHours(1), null);
    }
}
