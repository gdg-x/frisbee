package org.gdg.frisbee.android.eventseries;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Pair;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Event;
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
    void fetchEvents() {
        final DateTime now = DateTime.now();
        int mStart = (int) (now.minusMonths(2).dayOfMonth().withMinimumValue().getMillis() / 1000);
        int mEnd = (int) (now.plusYears(1).dayOfMonth().withMaximumValue().getMillis() / 1000);

        setIsLoading(true);
        final String plusId = getArguments().getString(Const.EXTRA_PLUS_ID);
        final String cacheKey = "event_" + plusId;


        if (Utils.isOnline(getActivity())) {
            App.getInstance().getGroupDirectory().getChapterEventList(mStart, mEnd, plusId).enqueue(new Callback<ArrayList<Event>>() {
                @Override
                public void success(ArrayList<Event> events) {
                    splitEventsAndAddToAdapter(events);
                    App.getInstance().getModelCache().putAsync(cacheKey, mEvents, DateTime.now().plusHours(2), new ModelCache.CachePutListener() {
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
            });
        } else {
            App.getInstance().getModelCache().getAsync(cacheKey, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {

                    if (checkValidCache(item)) {
                        ArrayList<Event> events = (ArrayList<Event>) item;
                        splitEventsAndAddToAdapter(events);
                        setIsLoading(false);
                        Snackbar snackbar = Snackbar.make(getView(), R.string.cached_content,
                                Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.info(snackbar).show();
                    } else {
                        App.getInstance().getModelCache().removeAsync(cacheKey);
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

    /**
     * Helper method that will split events into upcoming / past
     * and add to the adapter of this list fragment
     *
     * @param events
     *      The events list to be added
     */
    private void splitEventsAndAddToAdapter(Collection<? extends SimpleEvent> events) {
        Pair<List<SimpleEvent>, List<SimpleEvent>> pair = splitEventsList(events);
        mAdapter.addAll(pair.first);
        mAdapter.addAll(pair.second);
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
}
