package org.gdg.frisbee.android.gde;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.GdgPerson;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.widget.SquaredImageView;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

class PeopleAdapter extends ArrayAdapter<GdgPerson> {

    @DrawableRes private int placeholder;
    private HashMap<Integer, Object> mConsumedMap;

    public PeopleAdapter(Context ctx, @DrawableRes int placeholder) {
        super(ctx, R.layout.gde_item);
        this.placeholder = placeholder;
        mConsumedMap = new HashMap<>();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        return Utils.stringToLong(getItem(i).getUrl());
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final GdgPerson gdgPerson = getItem(i);

        View rowView = convertView;
        if (rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(R.layout.gde_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.nameView.setText(gdgPerson.getName());
        holder.countryView.setText(gdgPerson.getAddress());

        if (gdgPerson.getImageUrl() != null) {
            App.getInstance().getPicasso()
                    .load(gdgPerson.getImageUrl())
                    .placeholder(placeholder)
                    .into(holder.thumbnailView);
        }

        if (!mConsumedMap.containsKey(i)) {
            mConsumedMap.put(i, null);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(getContext());
            rowView.startAnimation(animation);
        }

        return rowView;
    }

    static class ViewHolder {
        @Bind(R.id.name) public TextView nameView;
        @Bind(R.id.country) public TextView countryView;
        @Bind(R.id.thumb) public SquaredImageView thumbnailView;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}
