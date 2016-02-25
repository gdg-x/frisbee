package org.gdg.frisbee.android;

import android.content.Context;
import android.graphics.Color;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ColorConfigAdapter extends WearableListView.Adapter {

    private final Context mContext;
    private final List<String> mColors;

    public ColorConfigAdapter(Context context, List<String> mColors) {
        mContext = context;
        this.mColors = mColors;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WearableListView.ViewHolder(new WearableListItemLayout(mContext));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        WearableListItemLayout layout = (WearableListItemLayout) holder.itemView;

        TextView nameTextView = (TextView) layout.findViewById(R.id.setting_text_view);
        nameTextView.setText(mColors.get(position));

        CircledImageView circleImage = (CircledImageView) layout.findViewById(R.id.setting_circle);
        String color = mColors.get(position);
        if ("Dark".equals(color)) {
            circleImage.setCircleColor(Color.BLACK);
        } else if ("Light".equals(color)) {
            circleImage.setCircleColor(Color.WHITE);
        } else {
            circleImage.setCircleColor(Color.parseColor(mColors.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return mColors.size();
    }
}
