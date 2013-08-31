/*
 * Copyright 2013 The GDG Frisbee Project
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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusShare;

import java.util.ArrayList;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.adapter.EventAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 23:10
 */
public abstract class EventFragment extends GdgListFragment implements View.OnClickListener {

    private static final String LOG_TAG = "GDG-EventFragment";
    protected GroupDirectory mClient;

    private int mSelectedMonth;
    protected EventAdapter mAdapter;

    protected ArrayList<SimpleEvent> mEvents;
    private ApiRequest mFetchEvents;
    protected DateTime mStart;
    protected DateTime mEnd;
    private PlusClient mPlusClient;

    Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            setIsLoading(false);
            Crouton.makeText(getActivity(), getString(R.string.fetch_events_failed), Style.ALERT).show();
        }
    };

    public static EventFragment newInstance(String plusId) {
        EventFragment fragment = new GdgEventFragment();
        Bundle arguments = new Bundle();
        arguments.putString("plus_id", plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSelectedMonth = DateTime.now().getMonthOfYear()+1;
        mClient = new GroupDirectory();

        mPlusClient = null;
        if (((GdgActivity) getActivity()).getPlayServicesHelper() != null) {
            mPlusClient = ((GdgActivity) getActivity()).getPlayServicesHelper().getPlusClient();
        }

        if(getListView() instanceof ListView) {
            ListView listView = (ListView) getListView();
            listView.setDivider(null);
            listView.setDividerHeight(0);
        }

        registerForContextMenu(getListView());


        mAdapter = new EventAdapter(getActivity(), this);
        setListAdapter(mAdapter);
        mEvents = new ArrayList<SimpleEvent>();

        fetchEvents();
    }

    abstract void fetchEvents();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Event event = (Event) mAdapter.getItem(info.position);
        menu.setHeaderTitle(event.getTitle());
        getActivity().getMenuInflater().inflate(R.menu.event_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Event event = (Event) mAdapter.getItem(info.position);

        switch(item.getItemId()) {
            case R.id.add_calendar:
                addEventToCalendar(event);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    public void addEventToCalendar(Event event) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");

        intent.putExtra("beginTime", event.getStart().getMillis());
        intent.putExtra("endTime", event.getEnd().getMillis());
        intent.putExtra("title", event.getTitle());
        intent.putExtra("eventLocation", event.getLocation());
        startActivity(intent);
    }

    private DateTime getMonthStart(int month) {
        MutableDateTime date = new MutableDateTime();
        date.setDayOfMonth(1);
        date.setMillisOfDay(0);
        date.setMonthOfYear(month);
        return date.toDateTime();
    }

    private DateTime getMonthEnd(int month) {
        MutableDateTime date = MutableDateTime.now();
        date.setMillisOfDay(0);
        date.setMonthOfYear(month);

        return date.toDateTime().dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, null);
    }

    @Override
    public void onClick(View view) {
        Event event = (Event) view.getTag();
        shareOnGplus(event);
    }

    private void shareOnGplus(Event event) {
        if (mPlusClient != null && mPlusClient.isConnected()) {
            PlusShare.Builder builder = new PlusShare.Builder(this.getActivity(), mPlusClient);

            Uri eventUri = Uri.parse("https://developers.google.com/events/" + event.getId() + "/");
            String eventDeepLinkId = getArguments().getString("plus_id") + "/events/" + event.getId();
            // Set call-to-action metadata.
            builder.addCallToAction(
                    "JOIN", /** call-to-action button label */
                    eventUri, /** call-to-action url (for desktop use) */
                    eventDeepLinkId +"/join" /** call to action deep-link ID (for mobile use), 512 characters or fewer */);

            // Set the content url (for desktop use).
            builder.setContentUrl(eventUri);

            // Set the target deep-link ID (for mobile use).
            builder.setContentDeepLinkId(eventDeepLinkId);

            // Set the share text.
            builder.setText(getString(R.string.join_me));

            startActivityForResult(builder.getIntent(), 0);
        } else {
            Crouton.makeText(getActivity(), R.string.signin_first, Style.INFO).show();
        }
    }
}
