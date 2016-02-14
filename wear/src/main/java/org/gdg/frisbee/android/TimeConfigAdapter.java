package org.gdg.frisbee.android;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TimeConfigAdapter extends WearableListView.Adapter {

    private final Context mContext;
    private final List<String> timeSettings;

    public TimeConfigAdapter(Context context, List<String> timeSettings) {
        mContext = context;
        this.timeSettings = timeSettings;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WearableListView.ViewHolder(new WearableListItemLayout(mContext));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        WearableListItemLayout layout = (WearableListItemLayout) holder.itemView;

        TextView nameTextView = (TextView) layout.findViewById(R.id.setting_text_view);
        nameTextView.setText(timeSettings.get(position));

        CircledImageView circleImage = (CircledImageView) layout.findViewById(R.id.setting_circle);
        circleImage.setImageResource(R.drawable.ic_time);
    }

    @Override
    public int getItemCount() {
        return timeSettings.size();
    }
}
