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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.PlusOneButton;

import java.util.HashMap;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.GdlShow;
import org.gdg.frisbee.android.api.model.GdlShowList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ResizableImageView;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 22:20
 * To change this template use File | Settings | File Templates.
 */
public class GdlAdapter extends BaseAdapter {

    private static final String LOG_TAG = "GDG-GdlAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private GdlShowList mShows;

    private HashMap<Integer,Object> mConsumedMap;
    private GoogleApiClient mPlusClient;

    public GdlAdapter(Context ctx, GoogleApiClient client) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mShows = new GdlShowList("");
        mPlusClient = client;
        mConsumedMap = new HashMap<Integer, Object>();
    }

    public void clear() {
        mShows.getShows().clear();
    }

    public void addAll(GdlShowList list) {
        mShows = list;
        notifyDataSetChanged();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return Utils.stringToLong(mShows.getShows().get(i).getUrl());
    }

    @Override
    public Object getItem(int i) {
        return mShows.getShows().get(i);
    }

    @Override
    public int getCount() {
        return mShows.getShows().size();
    }


    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if(rowView == null) {
            rowView = mInflater.inflate(R.layout.gdl_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.thumbnailView = (ResizableImageView) rowView.findViewById(R.id.thumb);
            viewHolder.titleView = (TextView) rowView.findViewById(R.id.title);
            viewHolder.plusButton = (PlusOneButton) rowView.findViewById(R.id.plus_one_button);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final GdlShow show = (GdlShow) getItem(i);

        holder.ytUrl = show.getYoutubeUrl();

        holder.thumbnailView.setImageDrawable(null);
        holder.thumbnailView.setBackgroundResource(R.drawable.gdl_video_dummy);
        App.getInstance().getPicasso()
                .load(show.getHighQualityThumbnail())
                .into(holder.thumbnailView);

        holder.titleView.setText(show.getTitle());

        if(mPlusClient != null) {
            holder.plusButton.setVisibility(View.VISIBLE);
            holder.plusButton.initialize(show.getYoutubeUrl(), 1);
        } else {
            holder.plusButton.setVisibility(View.GONE);
        }

        if (!mConsumedMap.containsKey(i)) {
            mConsumedMap.put(i, null);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(mContext);
            rowView.startAnimation(animation);
        }

        return rowView;
    }

    public void updatePlusOne(View v) {
        if(v != null && v.getTag() != null) {
            ViewHolder holder = (ViewHolder) v.getTag();

            if(mPlusClient != null && mPlusClient.isConnected()) {
                holder.plusButton.setVisibility(View.VISIBLE);
                holder.plusButton.initialize(holder.ytUrl, 1);
            }
        }
    }

    static class ViewHolder {
        public TextView titleView;
        public ResizableImageView thumbnailView;
        public PlusOneButton plusButton;
        public String ytUrl;
    }
}
