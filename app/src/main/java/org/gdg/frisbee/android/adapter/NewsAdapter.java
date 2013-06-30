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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusOneButton;
import com.google.api.services.plus.model.Activity;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.YoutubeActivity;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ResizableImageView;

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
    public int getViewTypeCount() {
        // nothing, article, video, photo, album, event
        return 6;
    }

    @Override
    public int getItemViewType(int position) {
        Item item = (Item) getItemInternal(position);
        Activity activity = item.getActivity();

        if(activity.getObject().getAttachments() == null || activity.getObject().getAttachments().isEmpty())
            return 0;
        else {
            Activity.PlusObject.Attachments attachment = activity.getObject().getAttachments().get(0);
            String objectType = attachment.getObjectType();

            if(objectType.equals("article"))
                return 1;
            else if(objectType.equals("video"))
                return 2;
            else if(objectType.equals("photo"))
                return 3;
            else if(objectType.equals("album"))
                return 4;
            else if(objectType.equals("event"))
                return 5;
        }
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view == null)
            view = mInflater.inflate(R.layout.news_item_base, null);

        Item item = (Item) getItemInternal(i);
        final Activity activity = item.getActivity();

        ViewGroup container = (ViewGroup) view.findViewById(R.id.attachmentContainer);

        view.setTag(activity.getId());
        PlusOneButton plusButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
        plusButton.initialize(mPlusClient, activity.getUrl(), 1);

        /*ResizableImageView  picture = (ResizableImageView) view.findViewById(R.id.image);
        picture.setOnClickListener(null);
        picture.setImageDrawable(null);*/

        if(activity.getVerb().equals("share"))
            populateShare(activity, view);
        else
            populatePost(activity, view);

        if(activity.getObject().getAttachments() != null && activity.getObject().getAttachments().size() > 0) {

            final Activity.PlusObject.Attachments attachment = activity.getObject().getAttachments().get(0);

            switch(getItemViewType(i)) {
                case 1:
                    // Article
                    populateArticle(container, attachment);
                    break;
                case 2:
                    // Video
                    populateVideo(container, attachment);
                    break;
                case 3:
                    // Photo
                    populatePhoto(container, attachment);
                    break;
                case 4:
                    // Album
                    populateAlbum(container, attachment);
                    break;
                case 5:
                    // Album
                    populateEvent(container, attachment);
                    break;
            }
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

    private View createAttachmentView(ViewGroup container, int layout) {
        View attachmentView = null;
        if(container.getChildCount() == 0) {
            attachmentView = mInflater.inflate(layout, null);
            container.addView(attachmentView);
        } else {
            attachmentView = container.getChildAt(0);
        }
        return attachmentView;
    }

    private void populateArticle(ViewGroup container, final Activity.PlusObject.Attachments attachment) {

        if(attachment == null)
            return;

        View attachmentView = createAttachmentView(container, R.layout.news_item_article);
        ImageView articleImage = (ImageView) attachmentView.findViewById(R.id.image);
        TextView title =  (TextView)attachmentView.findViewById(R.id.displayName);
        TextView content =  (TextView)attachmentView.findViewById(R.id.content);

        title.setText(attachment.getDisplayName());
        try {
            content.setText(new URL(attachment.getUrl()).getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(attachment.getImage() == null && attachment.getFullImage() == null)
            articleImage.setVisibility(View.GONE);
        else {
            String imageUrl = attachment.getImage().getUrl();
            if(attachment.getFullImage() != null)
                imageUrl = attachment.getFullImage().getUrl();

            articleImage.setImageDrawable(null);
            articleImage.setVisibility(View.VISIBLE);
            App.getInstance().getPicasso()
                    .load(imageUrl)
                    .into(articleImage);
        }

        attachmentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(attachment.getUrl()));
                mContext.startActivity(i);
            }
        });
    }

    private void populateVideo(ViewGroup container, final Activity.PlusObject.Attachments attachment) {

        if(attachment == null)
            return;

        View attachmentView = createAttachmentView(container, R.layout.news_item_video);
        ResizableImageView poster = (ResizableImageView) attachmentView.findViewById(R.id.videoPoster);
        App.getInstance().getPicasso()
                .load(attachment.getImage().getUrl())
                .into(poster);

        attachmentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent playVideoIntent = new Intent(mContext, YoutubeActivity.class);
                    playVideoIntent.putExtra("video_id", Utils.splitQuery(new URL(attachment.getUrl())).get("v"));
                    mContext.startActivity(playVideoIntent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void populatePhoto(ViewGroup container, Activity.PlusObject.Attachments attachment) {

        if(attachment == null)
            return;

        View attachmentView = createAttachmentView(container, R.layout.news_item_photo);

        ResizableImageView photo = (ResizableImageView) attachmentView.findViewById(R.id.photo);
        photo.setImageDrawable(null);

        App.getInstance().getPicasso()
                .load(attachment.getImage().getUrl())
                .into(photo);

    }

    private void populateAlbum(ViewGroup container, Activity.PlusObject.Attachments attachment) {

        if(attachment == null)
            return;

        View attachmentView = createAttachmentView(container, R.layout.news_item_album);

        ImageView pic1 = (ImageView) attachmentView.findViewById(R.id.pic1);
        ImageView pic2 = (ImageView) attachmentView.findViewById(R.id.pic2);
        ImageView pic3 = (ImageView) attachmentView.findViewById(R.id.pic3);

        App.getInstance().getPicasso()
                .load(attachment.getThumbnails().get(0).getImage().getUrl())
                .into(pic1);

        if(attachment.getThumbnails().size() > 1)
            App.getInstance().getPicasso()
                    .load(attachment.getThumbnails().get(1).getImage().getUrl())
                    .into(pic2);

        if(attachment.getThumbnails().size() > 2)
            App.getInstance().getPicasso()
                    .load(attachment.getThumbnails().get(2).getImage().getUrl())
                    .into(pic3);
    }

    private void populateEvent(ViewGroup container, Activity.PlusObject.Attachments attachment) {
        View attachmentView = createAttachmentView(container, R.layout.news_item_event);

        TextView content = (TextView) attachmentView.findViewById(R.id.content);
        content.setText(attachment.getContent());
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
