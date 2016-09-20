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

package org.gdg.frisbee.android.common;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.tasomaniac.android.widget.DelayedProgressBar;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.utils.EndlessScrollListener;

import butterknife.BindView;

public class GdgListFragment extends BaseFragment {

    private final Handler mHandler = new Handler();
    private final AdapterView.OnItemClickListener mOnClickListener =
        new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                onListItemClick(null, v, position, id);
            }
        };
    private final AbsListView.OnScrollListener mOnScrollListener =
        new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page) {
                return onListLoadMore(page);
            }
        };
    ListAdapter mAdapter;
    AbsListView mList;
    @BindView(R.id.empty)
    View mEmptyView;
    @BindView(R.id.loading)
    DelayedProgressBar mProgressContainer;
    CharSequence mEmptyText;
    boolean mListShown;
    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            getListView().focusableViewAvailable(mList);
        }
    };
    boolean mLoading;

    public GdgListFragment() {
    }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy <em>must</em> have a ListView whose id
     * is {@link android.R.id#list android.R.id.list} and can optionally
     * have a sibling view id {@link android.R.id#empty android.R.id.empty}
     * that is to be shown when the list is empty.
     * <p/>
     * <p>If you are overriding this method with your own custom content,
     * consider including the standard layout {@link android.R.layout#list_content}
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, R.layout.fragment_list, container);
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mListShown = false;
        mEmptyView = mProgressContainer = null;
        super.onDestroyView();
    }

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the
     * data associated with the selected item.
     *
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * This method will be called when new data needs to be
     * appended to the List. Subclasses should override.
     *
     * @param page              The page that should be loaded
     * @return true if more data is being loaded; false if there is no more
     */
    protected boolean onListLoadMore(int page) {
        return false;
    }

    private void updateEmpty() {
        if (!mLoading && getActivity() != null) {
            if (mAdapter == null || mAdapter.getCount() == 0) {
                setListShown(false, true);
                mEmptyView.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), android.R.anim.fade_in));
                mEmptyView.setVisibility(View.VISIBLE);

            } else {
                mEmptyView.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), android.R.anim.fade_out));
                mEmptyView.setVisibility(View.GONE);
                setListShown(true, true);
            }
        }
    }

    public void setIsLoading(boolean isLoading) {

        if (isLoading == mLoading || getActivity() == null) {
            return;
        }

        mLoading = isLoading;

        if (isLoading) {
            setListShown(false, true);
            mProgressContainer.show(true);
        } else {
            mProgressContainer.hide(true, new Runnable() {
                @Override
                public void run() {
                    if (mEmptyView != null) {
                        updateEmpty();
                    }
                }
            });
        }
    }

    /**
     * Set the currently selected list item to the specified
     * position with the adapter's data
     *
     * @param position
     */
    public void setSelection(int position) {
        ensureList();
        mList.setSelection(position);
    }

    /**
     * Get the position of the currently selected list item.
     */
    public int getSelectedItemPosition() {
        ensureList();
        return mList.getSelectedItemPosition();
    }

    /**
     * Get the cursor row ID of the currently selected list item.
     */
    public long getSelectedItemId() {
        ensureList();
        return mList.getSelectedItemId();
    }

    /**
     * Get the activity's list view widget.
     */
    public AdapterView<ListAdapter> getListView() {
        ensureList();
        return mList;
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     * <p/>
     * <p>Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with {@link #setListAdapter(ListAdapter)}.
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     *
     * @param shown If true, the list view is shown; if false, the progress
     *              indicator.  The initial value is true.
     */
    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    /**
     * Like {@link #setListShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     */
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown   If true, the list view is shown; if false, the progress
     *                indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     *                new state.
     */
    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }

        mListShown = shown;

        if (shown) {
            if (animate) {
                mList.setAlpha(0.0f);
                mList.animate().alpha(1.0f).setInterpolator(new DecelerateInterpolator());
            } else {
                mList.clearAnimation();
            }
            mList.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mList.setAlpha(1.0f);
                mList.animate().alpha(0.0f).setInterpolator(new AccelerateInterpolator());
            } else {
                mList.clearAnimation();
            }
            mList.setVisibility(View.GONE);
        }
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    /**
     * Provide the cursor for the list view.
     */
    public void setListAdapter(ListAdapter adapter) {
        mAdapter = adapter;
        if (mList != null) {
            mList.setAdapter(adapter);
        }
        updateEmpty();
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                updateEmpty();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();

                updateEmpty();
            }

        });
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }

        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }

        View rawList = root.findViewById(android.R.id.list);

        if (mEmptyView == null) {
            throw new RuntimeException(
                "Your content must have a View whose id attribute is 'R.id.empty'");
        }

        if (mProgressContainer == null) {
            throw new RuntimeException(
                "Your content must have a View whose id attribute is 'R.id.loading'");
        }
        mProgressContainer.setVisibility(View.GONE);

        if (rawList == null) {
            throw new RuntimeException(
                "Your content must have a ListView whose id attribute is 'R.id.list'");
        }

        if (!(rawList instanceof AbsListView)) {
            throw new RuntimeException(
                "Content has view with id attribute 'R.id.list' that is not a AbsListView class");
        }

        mList = (AbsListView) rawList;

        mListShown = true;
        mList.setOnItemClickListener(mOnClickListener);
        mList.setOnScrollListener(mOnScrollListener);

        if (mAdapter != null) {
            ListAdapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        } else {
            // We are starting without an adapter, so assume we won't
            // have our data right away and start with the progress indicator.
            if (mProgressContainer != null) {
                setListShown(false, false);
            }
        }
        mHandler.post(mRequestFocus);
    }
}
