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
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ResizableImageView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maui on 29.05.2014.
 */
public class GdeAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<Gde> mGdes;

    private HashMap<Integer,Object> mConsumedMap;
    private GoogleApiClient mPlusClient;

    public GdeAdapter(Context ctx, GoogleApiClient client) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mGdes = new ArrayList<Gde>();
        mPlusClient = client;
        mConsumedMap = new HashMap<Integer, Object>();
    }

    public void clear() {
        mGdes.clear();
    }

    public void addAll(ArrayList<? extends Gde> list) {
        mGdes.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return Utils.stringToLong(mGdes.get(i).getEmail());
    }

    @Override
    public Object getItem(int i) {
        return mGdes.get(i);
    }

    @Override
    public int getCount() {
        return mGdes.size();
    }


    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if(rowView == null) {
            rowView = mInflater.inflate(R.layout.gde_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.thumbnailView = (ResizableImageView) rowView.findViewById(R.id.thumb);
            viewHolder.nameView = (TextView) rowView.findViewById(R.id.name);
            viewHolder.countryView = (TextView) rowView.findViewById(R.id.country);
            viewHolder.plusButton = (PlusOneButton) rowView.findViewById(R.id.plus_one_button);
            rowView.setTag(viewHolder);
        }
        ViewHolder holder = (ViewHolder) rowView.getTag();
        final Gde gde = (Gde) getItem(i);

        holder.thumbnailView.setImageDrawable(null);
        holder.thumbnailView.setBackgroundResource(R.drawable.gdl_video_dummy);

        /*App.getInstance().getPicasso()
                .load(show.getHighQualityThumbnail())
                .into(holder.thumbnailView);*/

        holder.nameView.setText(gde.getName());
        holder.countryView.setText(gde.getAddress());

        if(mPlusClient != null) {
            holder.plusButton.setVisibility(View.VISIBLE);
            holder.plusButton.initialize(gde.getSocialUrl(), 1);
            holder.socialUrl = gde.getSocialUrl();
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
                holder.plusButton.initialize(holder.socialUrl, 1);
            }
        }
    }

    static class ViewHolder {
        public TextView nameView;
        public TextView countryView;
        public ResizableImageView thumbnailView;
        public PlusOneButton plusButton;
        public String socialUrl;
    }
}
