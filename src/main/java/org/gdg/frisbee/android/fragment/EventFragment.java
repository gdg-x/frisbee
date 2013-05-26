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
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.EventAdapter;
import org.gdg.frisbee.android.api.ApiException;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.util.ArrayList;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 23:10
 */
public class EventFragment extends GdgListFragment {

    private static final String LOG_TAG = "GDG-EventFragment";
    private GroupDirectory mClient;

    private int mSelectedMonth;
    private EventAdapter mAdapter;

    public static EventFragment newInstance(String plusId) {
        EventFragment fragment = new EventFragment();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSelectedMonth = DateTime.now().getMonthOfYear()+1;
        mClient = new GroupDirectory(getActivity());

        if(getListView() instanceof ListView) {
            ListView listView = (ListView) getListView();
            listView.setDivider(null);
            listView.setDividerHeight(0);
        }

        registerForContextMenu(getListView());

        mAdapter = new EventAdapter(getActivity());
        setListAdapter(mAdapter);

        new Builder<String, ArrayList>(String.class, ArrayList.class)
                .addParameter(getArguments().getString("plus_id"))
                .setOnPreExecuteListener(new CommonAsyncTask.OnPreExecuteListener() {
                    @Override
                    public void onPreExecute() {
                        setIsLoading(true);
                    }
                })
                .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, ArrayList>() {
                    @Override
                    public ArrayList<Event> doInBackground(String... params) {
                        try {

                            DateTime start = getMonthStart(mSelectedMonth);
                            DateTime end = getMonthStart(mSelectedMonth).plusDays(30);
                            ArrayList<Event> events = (ArrayList<Event>) App.getInstance().getModelCache().get("events_" + params[0] + "_" + start.getMillis() + "_" + end.getMillis());

                            if (events == null) {
                                events = mClient.getChapterEventList(start, end, params[0]);

                                App.getInstance().getModelCache().put("events_" + params[0] + "_" + start.getMillis() + "_" + end.getMillis(), events);
                            }

                            return events;
                        } catch (ApiException e) {
                            Log.e(LOG_TAG, "Fetching events failed", e);
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<ArrayList>() {
                    @Override
                    public void onPostExecute(ArrayList activityFeed) {
                        mAdapter.addAll(activityFeed);
                        setIsLoading(false);
                    }
                })
                .buildAndExecute();
    }

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
        intent.putExtra("endTime", event.getStart().getMillis());
        intent.putExtra("allDay", event.isAllDay());
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
}
