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
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusOneButton;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.GdgVolley;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.NetworkedCacheableImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * GDG Aachen
 * org.gdg.frisbee.android
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 02:48
 */
public class NewsAdapter extends BaseAdapter {

    private static final String LOG_TAG = "GDG-NewsAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Item> mActivities;

    private PlusClient mPlusClient;

    public NewsAdapter(Context ctx, PlusClient client) {
        mContext = ctx;
        mPlusClient = client;
        mInflater = LayoutInflater.from(mContext);
        mActivities = new ArrayList<Item>();
    }

    public void addAll(Collection<Activity> items) {
        for(Activity a : items) {
            mActivities.add(new Item(a));
        }
        notifyDataSetChanged();
    }

    public void add(Activity item) {
        mActivities.add(new Item(item));
        notifyDataSetChanged();
    }

    public void clear() {
        mActivities.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mActivities.size();
    }

    @Override
    public Object getItem(int i) {
        return mActivities.get(i).getActivity();
    }

    public Item getItemInternal(int i) {
        return mActivities.get(i);
    }

    @Override
    public long getItemId(int i) {
        return Utils.stringToLong(getItemInternal(i).getActivity().getId());
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Log.d(LOG_TAG, "get news item at "+ i);
        if(view == null)
            view = mInflater.inflate(R.layout.news_item, null);


        Item item = (Item) getItemInternal(i);
        Activity activity = item.getActivity();

        if(view.getTag() != null && view.getTag().equals(activity.getId())) {
            //return view;
        }

        view.setTag(activity.getId());
        PlusOneButton plusButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
        plusButton.initialize(mPlusClient, activity.getUrl(), 1);

        NetworkedCacheableImageView  picture = (NetworkedCacheableImageView) view.findViewById(R.id.image);
        //picture.setImageDrawable(null);

        if(activity.getVerb().equals("share"))
            populateShare(activity, view);
        else
            populatePost(activity, view);

        if(activity.getObject().getAttachments() != null &&
                activity.getObject().getAttachments().size() > 0 &&
                activity.getObject().getAttachments().get(0).getFullImage() != null &&
                activity.getObject().getAttachments().get(0).getFullImage().getUrl() != null &&
                activity.getObject().getAttachments().get(0).getFullImage().getUrl().length()>6) {
            String url = activity.getObject().getAttachments().get(0).getFullImage().getUrl();
            if(url.startsWith("//")) {
                url = "http:"+url;
            }

            Log.d(LOG_TAG, url);
            picture.setImageUrl(url, GdgVolley.getInstance().getImageLoader());
        } else if(activity.getObject().getAttachments() != null &&
                activity.getObject().getAttachments().size() > 0 &&
                activity.getObject().getAttachments().get(0).getImage() != null &&
                activity.getObject().getAttachments().get(0).getImage().getUrl() != null &&
                activity.getObject().getAttachments().get(0).getImage().getUrl().length()>6) {
            String url = activity.getObject().getAttachments().get(0).getImage().getUrl();
            if(url.startsWith("//")) {
                url = "http:"+url;
            }

            Log.d(LOG_TAG, url);
            picture.setImageUrl(url, GdgVolley.getInstance().getImageLoader());
        } else {
            picture.setImageDrawable(null);
        }

        // That item will contain a special property that tells if it was freshly retrieved
        if (!item.isConsumed()) {
            item.setConsumed(true);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(mContext);
            view.startAnimation(animation);
        }

        return view;
    }

    public void populatePost(Activity item, View v) {
        TextView content = (TextView) v.findViewById(R.id.content);
        content.setText(Html.fromHtml(item.getObject().getContent()));
    }

    public void populateShare(Activity item, View v) {
        TextView content = (TextView) v.findViewById(R.id.content);

        if(item.getAnnotation() != null) {
            content.setText(Html.fromHtml(item.getAnnotation() + "<hr/>"+item.getObject().getContent()));
        } else {
            content.setText(Html.fromHtml(item.getObject().getContent()));
        }
    }

    public class Item {
        private Activity mActivity;
        private boolean mConsumed = false;

        public Item(Activity a) {
            mActivity = a;
            mConsumed = false;
        }

        public Activity getActivity() {
            return mActivity;
        }

        public void setActivity(Activity mActivity) {
            this.mActivity = mActivity;
        }

        public boolean isConsumed() {
            return mConsumed;
        }

        public void setConsumed(boolean mConsumed) {
            this.mConsumed = mConsumed;
        }
    }
}
