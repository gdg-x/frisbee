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

package org.gdg.frisbee.android.eventseries;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.gdg.frisbee.android.common.GdgListFragment;
import org.gdg.frisbee.android.event.EventActivity;
import org.gdg.frisbee.android.view.ColoredSnackBar;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 23:10
 */
public abstract class EventListFragment extends GdgListFragment {

    protected EventAdapter mAdapter;

    protected ArrayList<SimpleEvent> mEvents;
    
    protected void onError(Throwable e) {
        setIsLoading(false);
        e.printStackTrace();
        if (isAdded()) {
            Snackbar snackbar = Snackbar.make(getView(), R.string.fetch_events_failed,
                    Snackbar.LENGTH_SHORT);
            ColoredSnackBar.alert(snackbar).show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getListView() instanceof ListView) {
            ListView listView = (ListView) getListView();
            listView.setDivider(null);
            listView.setDividerHeight(0);
        }

        registerForContextMenu(getListView());

        mAdapter = createEventAdapter();
        setListAdapter(mAdapter);
        mEvents = new ArrayList<>();

        fetchEvents();
    }

    abstract EventAdapter createEventAdapter();

    abstract void fetchEvents();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        SimpleEvent event = mAdapter.getItem(info.position);
        menu.setHeaderTitle(event.getTitle());
        getActivity().getMenuInflater().inflate(R.menu.event_context, menu);
        menu.findItem(R.id.navigate_to).setVisible(event.getLocation() != null);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        SimpleEvent event = mAdapter.getItem(info.position);

        switch (item.getItemId()) {
            case R.id.add_calendar:
                addEventToCalendar(event);
                return true;
            case R.id.navigate_to:
                launchNavigation(event);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        final ListView listView = (ListView) getListView();
        int fixedPosition = position - listView.getHeaderViewsCount();
        if (fixedPosition >= 0 && fixedPosition < mAdapter.getCount()) {
            SimpleEvent event = mAdapter.getItem(fixedPosition);

            Intent intent = new Intent(getActivity(), EventActivity.class);
            intent.putExtra(Const.EXTRA_EVENT_ID, event.getId());
            startActivity(intent);
        }
    }

    private void launchNavigation(SimpleEvent event) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + event.getLocation()));
        startActivity(intent);
    }

    public void addEventToCalendar(SimpleEvent event) {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");

        intent.putExtra("beginTime", event.getStart().getMillis());
        intent.putExtra("endTime", event.getEnd().getMillis());
        intent.putExtra("title", event.getTitle());

        String location = event.getLocation();
        if (location != null) {
            intent.putExtra("eventLocation", location);
        }

        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_events, container, false);
        ButterKnife.inject(this, v);
        return v;
    }
}
