package org.gdg.frisbee.android.eventseries;

import android.os.Bundle;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.util.ArrayList;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;

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
            App.getInstance().getGroupDirectory().getChapterEventList(mStart, mEnd, plusId, new Callback<ArrayList<Event>>() {
                @Override
                public void success(ArrayList<Event> events, retrofit.client.Response response) {
                    mEvents.addAll(events);

                    App.getInstance().getModelCache().putAsync(cacheKey, mEvents, DateTime.now().plusHours(2), new ModelCache.CachePutListener() {
                        @Override
                        public void onPutIntoCache() {
                            mAdapter.addAll(mEvents);
                            setIsLoading(false);
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    onError(error);
                }
            });
        } else {
            App.getInstance().getModelCache().getAsync(cacheKey, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ArrayList<Event> events = (ArrayList<Event>) item;

                    mAdapter.addAll(events);
                    setIsLoading(false);
                    Crouton.makeText(getActivity(), R.string.cached_content,
                            Style.INFO, R.id.content_frame).show();
                }

                @Override
                public void onNotFound(String key) {
                    setIsLoading(false);
                    Crouton.makeText(getActivity(), R.string.offline_alert,
                            Style.ALERT, R.id.content_frame).show();
                }
            });
        }
    }
}
