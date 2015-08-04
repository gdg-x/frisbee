package org.gdg.frisbee.android.eventseries;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;

public class GdgEventListFragment extends EventListFragment {

    @NonNull
    public static EventListFragment newInstance(String plusId) {
        EventListFragment fragment = new GdgEventListFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_PLUS_ID, plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
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
                public void success(@NonNull ArrayList<Event> events, retrofit.client.Response response) {
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
                public void failure(@NonNull RetrofitError error) {
                    onError(error);
                }
            });
        } else {
            App.getInstance().getModelCache().getAsync(cacheKey, false, new ModelCache.CacheListener() {
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
                    Snackbar snackbar = Snackbar.make(getView(), R.string.offline_alert,
                            Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.alert(snackbar).show();
                }
            });
        }
    }

    private boolean checkValidCache(Object item) {
        if (item instanceof ArrayList) {
            ArrayList<?> result = (ArrayList) item;
            if (result.size() > 0) {
                return result.get(0) instanceof SimpleEvent;
            }
        }
        return false;
    }
}
