/*
 * Copyright 2014 The GDG Frisbee Project
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

import android.os.Bundle;
import android.view.*;

import com.android.volley.Response;

import java.util.ArrayList;
import java.util.Comparator;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.EventAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.PagedList;
import org.gdg.frisbee.android.api.model.TaggedEvent;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.EventDateComparator;
import org.gdg.frisbee.android.utils.TaggedEventDistanceComparator;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class TaggedEventFragment extends EventFragment {

    private String mCacheKey = "";
    private String mEventTag = "";
    private Comparator<EventAdapter.Item> mLocationComparator = new TaggedEventDistanceComparator();
    private Comparator<EventAdapter.Item> mDateComparator = new EventDateComparator();
    private Comparator<EventAdapter.Item> mCurrentComparator = mLocationComparator;

    private DateTime mStart, mEnd;

    private Integer mFragmentLayout = null;

    public static TaggedEventFragment newInstance(String cacheKey, String eventTag, long start, long end, int fragmentLayout) {
        TaggedEventFragment frag = new TaggedEventFragment();
        Bundle args = new Bundle();
        args.putString(Const.SPECIAL_EVENT_CACHEKEY_EXTRA, cacheKey);
        args.putString(Const.SPECIAL_EVENT_VIEWTAG_EXTRA, eventTag);
        args.putLong(Const.SPECIAL_EVENT_START_EXTRA, start);
        args.putLong(Const.SPECIAL_EVENT_END_EXTRA, end);
        args.putInt(Const.SPECIAL_EVENT_FRAGMENT_LAYOUT_EXTRA, fragmentLayout);
        frag.setArguments(args);
        return frag;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            Bundle args = getArguments();
            mCacheKey = args.getString(Const.SPECIAL_EVENT_CACHEKEY_EXTRA);
            mEventTag = args.getString(Const.SPECIAL_EVENT_VIEWTAG_EXTRA);
            mStart = new DateTime(args.getLong(Const.SPECIAL_EVENT_START_EXTRA));
            mEnd = new DateTime(args.getLong(Const.SPECIAL_EVENT_END_EXTRA));

            mFragmentLayout = args.getInt(Const.SPECIAL_EVENT_FRAGMENT_LAYOUT_EXTRA, R.layout.fragment_events);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mFragmentLayout != 0) {
            View v = inflater.inflate(mFragmentLayout, null);
            ButterKnife.inject(this, v);
            return v;
        } else {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
    }

    @Override
    void fetchEvents() {
        setIsLoading(true);

        Response.Listener<PagedList<TaggedEvent>> listener = new Response.Listener<PagedList<TaggedEvent>>() {
            @Override
            public void onResponse(final PagedList<TaggedEvent> events) {
                mEvents.addAll(events.getItems());

                App.getInstance().getModelCache().putAsync(mCacheKey, mEvents, DateTime.now().plusHours(2), new ModelCache.CachePutListener() {
                    @Override
                    public void onPutIntoCache() {
                        mAdapter.addAll(mEvents);
                        sortEvents();
                        setIsLoading(false);
                    }
                });
            }
        };

        ApiRequest fetchEvents = mClient.getTaggedEventList(mStart, mEnd, mEventTag, listener, mErrorListener);

        if (Utils.isOnline(getActivity())) {
            fetchEvents.execute();
        } else {
            App.getInstance().getModelCache().getAsync(mCacheKey, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ArrayList<TaggedEvent> events = (ArrayList<TaggedEvent>) item;

                    mAdapter.addAll(events);
                    sortEvents();
                    setIsLoading(false);
                    if (isAdded())
                        Crouton.makeText(getActivity(), getString(R.string.cached_content), Style.INFO).show();
                }

                @Override
                public void onNotFound(String key) {
                    setIsLoading(false);
                    if (isAdded())
                        Crouton.makeText(getActivity(), getString(R.string.offline_alert), Style.ALERT).show();
                }
            });
        }
    }

    private void sortEvents() {
        mAdapter.sort(mCurrentComparator);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.special_event_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mCurrentComparator == mLocationComparator) {
            menu.findItem(R.id.order_by_date).setVisible(true);
            menu.findItem(R.id.order_by_distance).setVisible(false);
        } else {
            menu.findItem(R.id.order_by_distance).setVisible(true);
            menu.findItem(R.id.order_by_date).setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.order_by_date) {
            mCurrentComparator = mDateComparator;
            setIsLoading(true);
            sortEvents();
            setIsLoading(false);
            getActivity().supportInvalidateOptionsMenu();
            scrollToSoonestEvent();
            return true;
        } else if (item.getItemId() == R.id.order_by_distance) {
            mCurrentComparator = mLocationComparator;
            setIsLoading(true);
            sortEvents();
            setIsLoading(false);
            getActivity().supportInvalidateOptionsMenu();
            getListView().setSelection(0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scrollToSoonestEvent() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            if (mAdapter.getItem(i).getStart().getMillis() > now) {
                getListView().setSelection(i);
                return;
            }
        }
    }
}
