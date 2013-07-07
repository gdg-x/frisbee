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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusOneButton;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.YoutubeActivity;
import org.gdg.frisbee.android.api.model.GdlShow;
import org.gdg.frisbee.android.api.model.GdlShowList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ResizableImageView;

import java.util.ArrayList;
import java.util.HashMap;

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
    private PlusClient mPlusClient;

    public GdlAdapter(Context ctx, PlusClient client) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mShows = new GdlShowList("");
        mPlusClient = client;
        mConsumedMap = new HashMap<Integer, Object>();
    }

    public void clear() {
        mShows.clear();
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
        return Utils.stringToLong(mShows.get(i).getUrl());
    }

    @Override
    public Object getItem(int i) {
        return mShows.get(i);
    }

    @Override
    public int getCount() {
        return mShows.size();
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null)
            view = mInflater.inflate(R.layout.gdl_item, null);

        final GdlShow show = (GdlShow) getItem(i);
        ResizableImageView thumbnailView = (ResizableImageView) view.findViewById(R.id.thumb);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        view.setTag(show.getYoutubeUrl());

        View.OnClickListener mShowVideo = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent playVideoIntent = new Intent(mContext, YoutubeActivity.class);
                playVideoIntent.putExtra("gdl", true);
                playVideoIntent.putExtra("video_id", show.getYoutubeId());
                mContext.startActivity(playVideoIntent);
            }
        };

        thumbnailView.setBackgroundResource(R.drawable.gdl_video_dummy);
        App.getInstance().getPicasso()
                .load(show.getHighQualityThumbnail())
                .into(thumbnailView);

        titleView.setText(show.getTitle());

        thumbnailView.setOnClickListener(mShowVideo);
        titleView.setOnClickListener(mShowVideo);

        PlusOneButton plusButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
        if(mPlusClient != null) {
            plusButton.setVisibility(View.VISIBLE);
            plusButton.initialize(mPlusClient, show.getYoutubeUrl(), 1);
        } else {
            plusButton.setVisibility(View.GONE);
        }

        if (!mConsumedMap.containsKey(i)) {
            mConsumedMap.put(i, null);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(mContext);
            view.startAnimation(animation);
        }

        return view;
    }

    public void updatePlusOne(View v) {
        if(v != null && v.getTag() != null) {
            String url = (String) v.getTag();
            PlusOneButton plusButton = (PlusOneButton) v.findViewById(R.id.plus_one_button);

            if(mPlusClient != null) {
                plusButton.setVisibility(View.VISIBLE);
                plusButton.initialize(mPlusClient, url, 1);
            }
        }
    }
}
