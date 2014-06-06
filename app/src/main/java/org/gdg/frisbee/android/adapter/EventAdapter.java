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

package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.SimpleEvent;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.event.EventActivity;
import org.gdg.frisbee.android.view.SquaredImageView;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.adapter
 * <p/>
 * User: maui
 * Date: 23.04.13
 * Time: 03:53
 */
public class EventAdapter extends BaseAdapter {

    private final View.OnClickListener shareClickListener;
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Item> mEvents;

    public EventAdapter(Context ctx, View.OnClickListener shareClickListener) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mEvents = new ArrayList<Item>();
        this.shareClickListener = shareClickListener;
    }

    public void addAll(Collection<? extends SimpleEvent> items) {
        for(SimpleEvent a : items) {
            mEvents.add(new Item(a));
        }
        notifyDataSetChanged();
    }

    public void add(SimpleEvent item) {
        mEvents.add(new Item(item));
        notifyDataSetChanged();
    }

    public void clear() {
        mEvents.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public SimpleEvent getItem(int i) {
        return mEvents.get(i).getEvent();
    }

    private Item getItemInternal(int i) {
        return mEvents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.list_event_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.line2 = (TextView) rowView.findViewById(R.id.line2);
            viewHolder.shareButton = (ImageButton) rowView.findViewById(R.id.share_button);
            viewHolder.past = (TextView) rowView.findViewById(R.id.past);
            viewHolder.icon = (SquaredImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        Item item = (Item) getItemInternal(i);
        final SimpleEvent event = item.getEvent();

        if (event.getIconUrl() != null){
            holder.icon.setVisibility(View.VISIBLE);
            App.getInstance().getPicasso()
                .load(Const.URL_DEVELOPERS_GOOGLE_COM + event.getIconUrl())
                .into(holder.icon);
        } else {
            holder.icon.setVisibility(View.GONE);
        }

        holder.title.setText(event.getTitle());

        holder.line2.setText(event.getStart().toLocalDateTime().toString(DateTimeFormat.patternForStyle("MS",mContext.getResources().getConfiguration().locale)));

        if(event.getStart().isBefore(DateTime.now())) {
            holder.past.setVisibility(View.VISIBLE);
            holder.shareButton.setVisibility(View.GONE);
        } else {
            holder.shareButton.setOnClickListener(shareClickListener);
            holder.shareButton.setTag(event);
            holder.past.setVisibility(View.GONE);
            holder.shareButton.setVisibility(View.VISIBLE);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, EventActivity.class);
                intent.putExtra(Const.EXTRA_EVENT_ID, event.getId());
                mContext.startActivity(intent);
            }
        });

        rowView.setLongClickable(true);
        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                view.showContextMenu();
                return true;
            }
        });

        // That item will contain a special property that tells if it was freshly retrieved
        if (!item.isConsumed()) {
            item.setConsumed(true);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(mContext);
            rowView.startAnimation(animation);
        }


        return rowView;
    }

    private void openEventLink(SimpleEvent event) {
        String link = event.getGPlusEventLink();
        if (!TextUtils.isEmpty(link)){
            if (!link.startsWith("http")){
                link = "https://" + link;
            }
            openEventInExternalApp(link);
        } else {
            link = event.getLink();
            if (!TextUtils.isEmpty(link)) {
                if (!link.startsWith("http")) {
                    link = Const.URL_DEVELOPERS_GOOGLE_COM + link;
                }
                openEventInExternalApp(link);
            }
        }
    }

    public void openEventInExternalApp(String uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(uri));
        mContext.startActivity(i);
    }

    public void sort(Comparator<Item> eventComparator){
        Collections.sort(mEvents, eventComparator);
        notifyDataSetChanged();
    }

    public class Item {
        private SimpleEvent mEvent;
        private boolean mConsumed = false;

        public Item(SimpleEvent a) {
            mEvent = a;
            mConsumed = false;
        }

        public boolean isConsumed() {
            return mConsumed;
        }

        public void setConsumed(boolean mConsumed) {
            this.mConsumed = mConsumed;
        }

        public SimpleEvent getEvent() {
            return mEvent;
        }

        public void setEvent(Event mEvent) {
            this.mEvent = mEvent;
        }
    }

    static class ViewHolder {
        public TextView title, line2, past;
        public ImageButton shareButton;
        public SquaredImageView icon;
    }
}