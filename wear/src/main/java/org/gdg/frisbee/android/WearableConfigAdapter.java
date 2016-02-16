package org.gdg.frisbee.android;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class WearableConfigAdapter extends WearableListView.Adapter {

    private Context mContext;
    private List<WearableConfiguration> mConfigurations;

    public WearableConfigAdapter(Context context, List<WearableConfiguration> mConfigurations) {
        mContext = context;
        this.mConfigurations = mConfigurations;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WearableListView.ViewHolder(new WearableListItemLayout(mContext));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        WearableListItemLayout layout = (WearableListItemLayout) holder.itemView;

        WearableConfiguration configuration = mConfigurations.get(position);

        TextView nameTextView = (TextView) layout.findViewById(R.id.setting_text_view);
        nameTextView.setText(configuration.getTitle());

        TextView settingTextView = (TextView) layout.findViewById(R.id.subsetting_text_view);
        settingTextView.setVisibility("Date".equals(nameTextView.getText().toString()) ? View.VISIBLE : View.GONE);
        settingTextView.setText(configuration.isSelected() ?
            mContext.getString(R.string.label_setting_on) : mContext.getString(R.string.label_setting_off));

        CircledImageView circleImage = (CircledImageView) layout.findViewById(R.id.setting_circle);
        circleImage.setImageResource(configuration.getIcon());
    }

    @Override
    public int getItemCount() {
        return mConfigurations.size();
    }

    public void setConfigurations(List<WearableConfiguration> mConfigurations) {
        this.mConfigurations = mConfigurations;
        notifyDataSetChanged();
    }
}
