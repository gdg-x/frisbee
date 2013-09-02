package org.gdg.frisbee.android.fragment;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Response;

import java.util.ArrayList;
import java.util.Comparator;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.EventAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.model.TaggedEvent;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.EventDateComparator;
import org.gdg.frisbee.android.utils.TaggedEventDistanceComparator;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class DevFestEventFragment extends EventFragment {
    private static final String CACHE_KEY = "devfest2013";
    private Comparator<EventAdapter.Item> mLocationComparator = new TaggedEventDistanceComparator();
    private Comparator<EventAdapter.Item> mDateComparator = new EventDateComparator();
    private Comparator<EventAdapter.Item> mCurrentComparator = mLocationComparator;

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
                        sortEvents();
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

    private void sortEvents() {
        mAdapter.sort(mCurrentComparator);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.devfest_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mCurrentComparator == mLocationComparator){
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
            sortEvents();
            return true;
        } else if (item.getItemId() == R.id.order_by_distance) {
            mCurrentComparator = mLocationComparator;
            sortEvents();
            return true;
        }
        return false;
    }
}
