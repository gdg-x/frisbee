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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
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
import org.joda.time.DateTime;


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
    private GoogleApiClient mPlusClient;

    public NewsAdapter(Context ctx, GoogleApiClient client) {
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

    public void replaceAll(Collection<Activity> items, int start) {
        for(Activity a : items) {

            if(start < mActivities.size()) {
                if(a.getId().equals(mActivities.get(start).getActivity().getId())) {
                    mActivities.set(start, new Item(a));
                } else {
                    mActivities.add(start, new Item(a));
                    start++;
                }
            } else {
                mActivities.add(new Item(a));
            }

            start++;
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
        Item item = getItemInternal(position);
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

    public void updatePlusOne(View v) {
        if(v != null && v.getTag() != null) {
            ViewHolder viewHolder = (ViewHolder) v.getTag();

            if(mPlusClient != null && mPlusClient.isConnected()) {
                viewHolder.plusButton.setVisibility(View.VISIBLE);
                viewHolder.plusButton.initialize(viewHolder.url, 1);
            }
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder mViewHolder;
        if (view == null) {
            view = mInflater.inflate(R.layout.news_item_base, null);

            mViewHolder = new ViewHolder();
            mViewHolder.plusButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
            mViewHolder.container = (ViewGroup) view.findViewById(R.id.attachmentContainer);
            mViewHolder.timeStamp = (TextView) view.findViewById(R.id.timestamp);
            mViewHolder.content =  (TextView) view.findViewById(R.id.content);
            mViewHolder.shareContent =  (TextView) view.findViewById(R.id.shareContent);
            mViewHolder.shareContainer =  (ViewGroup) view.findViewById(R.id.shareContainer);
            mViewHolder.content.setMovementMethod(LinkMovementMethod.getInstance());
            mViewHolder.shareContent.setMovementMethod(LinkMovementMethod.getInstance());
            view.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) view.getTag();
        }

        Item item = getItemInternal(i);
        final Activity activity = item.getActivity();

        mViewHolder.url = activity.getUrl();

        PlusOneButton plusButton = (PlusOneButton) view.findViewById(R.id.plus_one_button);
        if(mPlusClient != null && mPlusClient.isConnected()) {
            mViewHolder.plusButton.setVisibility(View.VISIBLE);
            mViewHolder.plusButton.initialize(activity.getUrl(), 1);
        } else {
            mViewHolder.plusButton.setVisibility(View.GONE);
        }

        if(activity.getPublished() != null) {
            mViewHolder.timeStamp.setVisibility(View.VISIBLE);
            mViewHolder.timeStamp.setText(Utils.toHumanTimePeriod(mContext,new DateTime(activity.getPublished().getValue()), DateTime.now()));
        } else {
            mViewHolder.timeStamp.setVisibility(View.GONE);
        }

        if (activity.getVerb().equals("share"))
            populateShare(activity, mViewHolder);
        else {
            mViewHolder.shareContainer.setVisibility(View.GONE);
            populatePost(activity, mViewHolder.content);
        }

        if(activity.getObject().getAttachments() != null && activity.getObject().getAttachments().size() > 0) {

            final Activity.PlusObject.Attachments attachment = activity.getObject().getAttachments().get(0);

            switch(getItemViewType(i)) {
                case 1:
                    // Article
                    populateArticle(mViewHolder, mViewHolder.container, attachment);
                    break;
                case 2:
                    // Video
                    populateVideo(mViewHolder, mViewHolder.container, attachment);
                    break;
                case 3:
                    // Photo
                    populatePhoto(mViewHolder, mViewHolder.container, attachment);
                    break;
                case 4:
                    // Album
                    populateAlbum(mViewHolder, mViewHolder.container, attachment);
                    break;
                case 5:
                    // Event
                    populateEvent(mViewHolder, mViewHolder.container, attachment);
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

    private View createAttachmentView(ViewHolder mViewHolder, ViewGroup container, int layout, int type) {
        View attachmentView;

        if (container.getChildCount() == 0) {
            attachmentView = mInflater.inflate(layout, null);
            container.addView(attachmentView);

            switch(type) {
                case 1:
                    // Article
                    mViewHolder.articleImage = (ImageView) attachmentView.findViewById(R.id.image);
                    mViewHolder.title =  (TextView) attachmentView.findViewById(R.id.displayName);
                    mViewHolder.attachmentContent =  (TextView) attachmentView.findViewById(R.id.content);
                    break;
                case 2:
                    // Video
                    mViewHolder.poster = (ResizableImageView) attachmentView.findViewById(R.id.videoPoster);
                    break;
                case 3:
                    // Photo
                    mViewHolder.photo = (ResizableImageView) attachmentView.findViewById(R.id.photo);
                    break;
                case 4:
                    // Album
                    mViewHolder.pic1 = (ImageView) attachmentView.findViewById(R.id.pic1);
                    mViewHolder.pic2 = (ImageView) attachmentView.findViewById(R.id.pic2);
                    mViewHolder.pic3 = (ImageView) attachmentView.findViewById(R.id.pic3);
                    break;
                case 5:
                    // Event
                    mViewHolder.attachmentContent =  (TextView) attachmentView.findViewById(R.id.content);
                    mViewHolder.attachmentTitle = (TextView) attachmentView.findViewById(R.id.title);
                    mViewHolder.articleImage = (ImageView) attachmentView.findViewById(R.id.image);
                    break;
            }
        } else {
            attachmentView = container.getChildAt(0);
        }

        return attachmentView;
    }

    private void populateArticle(ViewHolder mViewHolder, ViewGroup container, final Activity.PlusObject.Attachments attachment) {
        if(attachment == null)
            return;

        View attachmentView = createAttachmentView(mViewHolder, container, R.layout.news_item_article, 1);

        mViewHolder.title.setText(attachment.getDisplayName());
        try {
            mViewHolder.attachmentContent.setText(new URL(attachment.getUrl()).getHost());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if(attachment.getImage() == null && attachment.getFullImage() == null)
            mViewHolder.articleImage.setVisibility(View.GONE);
        else {
            String imageUrl = attachment.getImage().getUrl();
            if(attachment.getFullImage() != null)
                imageUrl = attachment.getFullImage().getUrl();

            mViewHolder.articleImage.setImageDrawable(null);
            mViewHolder.articleImage.setVisibility(View.VISIBLE);
            App.getInstance().getPicasso()
                    .load(imageUrl)
                    .into(mViewHolder.articleImage);
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

    private void populateVideo(ViewHolder mViewHolder, ViewGroup container, final Activity.PlusObject.Attachments attachment) {
        if(attachment == null)
            return;

        View attachmentView = createAttachmentView(mViewHolder, container, R.layout.news_item_video, 2);

        // Precalc Image Size
        mViewHolder.poster.setDimensions(attachment.getImage().getWidth(), attachment.getImage().getHeight(), attachment.getImage().getUrl());
        mViewHolder.poster.setImageDrawable(null);

        App.getInstance().getPicasso()
                .load(attachment.getImage().getUrl())
                .into(mViewHolder.poster);

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

    private void populatePhoto(ViewHolder mViewHolder, ViewGroup container, Activity.PlusObject.Attachments attachment) {
        if(attachment == null)
            return;

        createAttachmentView(mViewHolder, container, R.layout.news_item_photo, 3);

        // Precalc Image Size
        if(attachment.getImage() != null && attachment.getImage().getUrl() != null && attachment.getImage().getWidth() != null)
            mViewHolder.photo.setDimensions(attachment.getImage().getWidth(), attachment.getImage().getHeight(), attachment.getImage().getUrl());

        mViewHolder.photo.setImageDrawable(null);

        App.getInstance().getPicasso()
                .load(attachment.getImage().getUrl())
                .into(mViewHolder.photo);

    }

    private void populateAlbum(ViewHolder mViewHolder, ViewGroup container, Activity.PlusObject.Attachments attachment) {
        if(attachment == null)
            return;

        createAttachmentView(mViewHolder, container, R.layout.news_item_album, 4);

        App.getInstance().getPicasso()
                .load(attachment.getThumbnails().get(0).getImage().getUrl())
                .into(mViewHolder.pic1);

        if(attachment.getThumbnails().size() > 1)
            App.getInstance().getPicasso()
                    .load(attachment.getThumbnails().get(1).getImage().getUrl())
                    .into(mViewHolder.pic2);

        if(attachment.getThumbnails().size() > 2)
            App.getInstance().getPicasso()
                    .load(attachment.getThumbnails().get(2).getImage().getUrl())
                    .into(mViewHolder.pic3);
    }

    private void populateEvent(ViewHolder mViewHolder, ViewGroup container, final Activity.PlusObject.Attachments attachment) {
        createAttachmentView(mViewHolder, container, R.layout.news_item_event, 5);

        TextView title = mViewHolder.attachmentTitle;
        String name = attachment.getDisplayName();
        if (TextUtils.isEmpty(name)){
            title.setVisibility(View.GONE);
        } else {
            title.setText(name);
            title.setClickable(true);
            View.OnClickListener mClickEvent = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openEventInGPlus(attachment.getUrl());
                }
            };

            mViewHolder.articleImage.setOnClickListener(mClickEvent);
            title.setOnClickListener(mClickEvent);
        }

        TextView content = mViewHolder.attachmentContent;
        content.setText(attachment.getContent());
    }

    public void openEventInGPlus(String uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(uri));
        mContext.startActivity(i);

    }

    private void populatePost(Activity item, TextView content) {
        content.setText(fromHtml(item.getObject().getContent()));
    }

    private void populateShare(Activity item, ViewHolder holder) {
        String originallyShared = "";

        if(item.getObject().getActor() != null && mContext != null)
            originallyShared = "<b><a href=\""+item.getObject().getActor().getUrl()+"\">"+ item.getObject().getActor().getDisplayName() +"</a></b> "+ mContext.getString(R.string.originally_shared)+"<br/><br/>";

        if (item.getAnnotation() != null) {
            holder.content.setText(fromHtml(item.getAnnotation()));
            holder.shareContent.setText(fromHtml(originallyShared + item.getObject().getContent()));
            holder.shareContainer.setVisibility(View.VISIBLE);
        } else {
            holder.shareContainer.setVisibility(View.GONE);
            holder.content.setText(fromHtml(originallyShared + item.getObject().getContent()));
        }
    }

    private Spanned fromHtml(String html) {
        Spanned spanned = Html.fromHtml(html);

        if(spanned instanceof SpannableStringBuilder) {
            SpannableStringBuilder ssb = (SpannableStringBuilder) spanned;

            URLSpan[] urlspans = ssb.getSpans(0, ssb.length()-1, URLSpan.class);
            for(int i = 0; i < urlspans.length; i++) {
                URLSpan span = urlspans[i];
                int start = ssb.getSpanStart(span);
                int end = ssb.getSpanEnd(span);
                final String url = span.getURL();

                ssb.removeSpan(span);

                ssb.setSpan(new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        mContext.startActivity(i);
                    }
                }, start, end, 33);
            }
        }
        return spanned;
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

    private class ViewHolder {
        PlusOneButton plusButton;
        ViewGroup container;
        ViewGroup shareContainer;
        TextView timeStamp;
        ImageView articleImage;
        TextView title;
        ResizableImageView poster;
        ResizableImageView photo;
        TextView attachmentContent;
        TextView content;
        TextView shareContent;
        ImageView pic1;
        ImageView pic2;
        ImageView pic3;
        String url;
        public TextView attachmentTitle;
    }
}
