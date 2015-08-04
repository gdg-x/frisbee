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

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.BaseFragment;

import butterknife.ButterKnife;
import butterknife.Bind;

@SuppressWarnings("unused")
public class GdgRecyclerFragment extends BaseFragment {

    private final Handler mHandler = new Handler();

    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            getListView().focusableViewAvailable(mList);
        }
    };

    RecyclerView.Adapter mAdapter;

    RecyclerView mList;

    @Bind(R.id.empty)
    View mEmptyView;

    @Bind(R.id.loading)
    View mProgressContainer;

    CharSequence mEmptyText;
    boolean mListShown;

    boolean mLoading;

    public GdgRecyclerFragment() {
    }

    /**
     * Provide default implementation to return a simple list view.  Subclasses
     * can override to replace with their own layout.  If doing so, the
     * returned view hierarchy <em>must</em> have a ListView whose id
     * is {@link android.R.id#list android.R.id.list} and can optionally
     * have a sibling view id {@link android.R.id#empty android.R.id.empty}
     * that is to be shown when the list is empty.
     *
     * <p>If you are overriding this method with your own custom content,
     * consider including the standard layout {@link android.R.layout#list_content}
     * in your layout file, so that you continue to retain all of the standard
     * behavior of ListFragment.  In particular, this is currently the only
     * way to have the built-in indeterminant progress state be shown.
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recycler_view, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureRecyclerView();
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
     * Provide the cursor for the list view.
     */
    public void setRecyclerAdapter(@NonNull RecyclerView.Adapter adapter) {
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mList != null) {
            mList.setAdapter(adapter);
            if (!mListShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter.  It is now time to show it.
                setListShown(true, getView() != null && getView().getWindowToken() != null);
            }
        }
        updateEmpty();
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();

                updateEmpty();
            }

//            @Override
//            public void onInvalidated() {
//                super.onInvalidated();
//
//                updateEmpty();
//            }

        });
    }

    private void updateEmpty() {
        if (!mLoading && getActivity() != null) {
            if (mAdapter == null || mAdapter.getItemCount() == 0) {
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
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), android.R.anim.fade_in));
            mProgressContainer.setVisibility(View.VISIBLE);
        } else {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mProgressContainer != null) {
                        mProgressContainer.setVisibility(View.GONE);
                    }
                    updateEmpty();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mProgressContainer.startAnimation(fadeOut);
        }
    }


    /**
     * Get the activity's list view widget.
     */
    public RecyclerView getListView() {
        ensureRecyclerView();
        return mList;
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * <p>Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with .
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     *
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
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
     * @param shown If true, the list view is shown; if false, the progress
     * indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     * new state.
     */
    private void setListShown(boolean shown, boolean animate) {
        ensureRecyclerView();
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }

        mListShown = shown;

        if (shown) {
            if (animate) {
                mList.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mList.clearAnimation();
            }
            mList.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mList.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mList.clearAnimation();
            }
            mList.setVisibility(View.GONE);
        }
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
    @Nullable
    public RecyclerView.Adapter getAdapter() {
        return mAdapter;
    }

    private void ensureRecyclerView() {
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
                    "Your content must have a RecyclerView whose id attribute is 'R.id.list'");
        }

        if (!(rawList instanceof RecyclerView)) {
            throw new RuntimeException(
                    "Content has view with id attribute 'R.id.list' that is not a RecyclerView class");
        }

        mList = (RecyclerView) rawList;

        mListShown = true;

        if (mAdapter != null) {
            RecyclerView.Adapter adapter = mAdapter;
            mAdapter = null;
            setRecyclerAdapter(adapter);
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
