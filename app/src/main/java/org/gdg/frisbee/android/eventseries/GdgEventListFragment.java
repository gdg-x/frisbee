package org.gdg.frisbee.android.eventseries;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Pair;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.PagedList;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GdgEventListFragment extends EventListFragment {

    private String mCacheKey;
    private String mPlusId;

    public static EventListFragment newInstance(String plusId) {
        EventListFragment fragment = new GdgEventListFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_PLUS_ID, plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    EventAdapter createEventAdapter() {
        return new EventAdapter(getActivity());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPlusId = getArguments().getString(Const.EXTRA_PLUS_ID);
        mCacheKey = "event_" + mPlusId;
    }

    @Override
    void fetchEvents() {
        setIsLoading(true);

        if (Utils.isOnline(getActivity())) {
            loadFirstPage();
        } else {
            App.getInstance().getModelCache().getAsync(mCacheKey, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {

                    if (checkValidCache(item)) {
                        ArrayList<Event> events = (ArrayList<Event>) item;
                        mAdapter.addAll(events);
                        setIsLoading(false);
                        Snackbar snackbar = Snackbar.make(getView(), R.string.cached_content,
                                Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.info(snackbar).show();
                    } else {
                        App.getInstance().getModelCache().removeAsync(mCacheKey);
                        onNotFound();
                    }
                }

                @Override
                public void onNotFound(String key) {
                    onNotFound();
                }

                private void onNotFound() {
                    setIsLoading(false);
                    showError(R.string.offline_alert);
                }
            });
        }
    }

    private void loadFirstPage() {
        onListLoadMore(1);
    }

    /**
     * Split a events list into Upcoming / Past events
     *
     * @param events
     *      The events list that needs to be sorted
     * @return
     *      A pair containting the split list into Upcoming / Past events
     */
    private Pair<List<SimpleEvent>, List<SimpleEvent>> splitEventsList(Collection<? extends SimpleEvent> events) {
        List<SimpleEvent> upcoming = new ArrayList<>();
        List<SimpleEvent> past = new ArrayList<>();

        DateTime now = DateTime.now();

        for (SimpleEvent event : events) {
            if (event.getStart().isBefore(now)) {
                past.add(event);
            } else {
                upcoming.add(event);
            }
        }
        Collections.reverse(past);
        return new Pair<>(upcoming, past);
    }

    @Override
    protected boolean onListLoadMore(int page) {
        App.getInstance().getGdgXHub().getChapterAllEventList(
            mPlusId, page).
            enqueue(new Callback<PagedList<Event>>() {
                @Override
                public void success(PagedList<Event> eventsPagedList) {
                    List<Event> events = eventsPagedList.getItems();
                    mAdapter.addAll(events);
                    App.getInstance().getModelCache().putAsync(mCacheKey,
                        mEvents,
                        DateTime.now().plusHours(2),
                        new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                mAdapter.addAll(mEvents);
                                setIsLoading(false);
                            }
                        });
                }

                @Override
                public void failure(Throwable error) {
                    onError(R.string.fetch_events_failed);
                }

                @Override
                public void networkFailure(Throwable error) {
                    onError(R.string.offline_alert);
                }
            }
        );
        return true;
    }
}
