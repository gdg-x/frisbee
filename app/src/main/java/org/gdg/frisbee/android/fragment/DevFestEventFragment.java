package org.gdg.frisbee.android.fragment;

import com.android.volley.Response;

import java.util.ArrayList;

import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.gdg.frisbee.android.api.model.TaggedEvent;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import org.gdg.frisbee.android.R;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DevFestEventFragment extends EventFragment {
    private static final String CACHE_KEY = "devfest2013";

    @Override
    void fetchEvents() {
        mStart = new DateTime(2013, 9, 13, 0, 0);
        mEnd = new DateTime(2013, 11, 10, 0, 0);

        setIsLoading(true);

        Response.Listener<ArrayList<TaggedEvent>> listener = new Response.Listener<ArrayList<TaggedEvent>>() {
            @Override
            public void onResponse(final ArrayList<TaggedEvent> events) {
                mEvents.addAll(events);

                App.getInstance().getModelCache().putAsync(CACHE_KEY, mEvents, DateTime.now().plusHours(2), new ModelCache.CachePutListener() {
                    @Override
                    public void onPutIntoCache() {
                        mAdapter.addAll(mEvents);
                        setIsLoading(false);
                    }
                });
            }
        };

        ApiRequest fetchEvents = mClient.getTaggedEventList(mStart, mEnd, "devfest", listener, mErrorListener);

        if (Utils.isOnline(getActivity())) {
            fetchEvents.execute();
        } else {
            App.getInstance().getModelCache().getAsync(CACHE_KEY, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ArrayList<TaggedEvent> events = (ArrayList<TaggedEvent>) item;

                    mAdapter.addAll(events);
                    setIsLoading(false);
                    Crouton.makeText(getActivity(), getString(R.string.cached_content), Style.INFO).show();
                }

                @Override
                public void onNotFound(String key) {
                    setIsLoading(false);
                    Crouton.makeText(getActivity(), getString(R.string.offline_alert), Style.ALERT).show();
                }
            });
        }
    }

}
