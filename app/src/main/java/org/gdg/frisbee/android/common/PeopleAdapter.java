package org.gdg.frisbee.android.common;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.GdgPerson;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.widget.SquaredImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PeopleAdapter extends ArrayAdapter<GdgPerson> {

    private final Picasso picasso;
    @DrawableRes
    private int placeholder;
    private SparseBooleanArray mConsumedMap;

    public PeopleAdapter(Context context, Picasso picasso) {
        this(context, picasso, 0);
    }

    public PeopleAdapter(Context context, Picasso picasso, @DrawableRes int placeholder) {
        super(context, R.layout.list_person_item);
        this.picasso = picasso;
        this.placeholder = placeholder;
        mConsumedMap = new SparseBooleanArray();
    }

    public void setPlaceholder(@DrawableRes int placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int i) {
        if (getItem(i).getUrl() != null) {
            return Utils.stringToLong(getItem(i).getUrl());
        } else {
            return Utils.stringToLong(getItem(i).getPrimaryText());
        }
    }

    @NonNull
    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        final GdgPerson gdgPerson = getItem(i);

        View rowView = convertView;
        if (rowView == null) {
            rowView = LayoutInflater.from(getContext()).inflate(R.layout.list_person_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }
        final ViewHolder holder = (ViewHolder) rowView.getTag();

        holder.primaryTextView.setText(gdgPerson.getPrimaryText());
        holder.secondaryTextView.setText(gdgPerson.getSecondaryText());

        if (!TextUtils.isEmpty(gdgPerson.getImageUrl())) {
            RequestCreator creator = picasso
                .load(gdgPerson.getImageUrl());
            if (placeholder != 0) {
                creator.placeholder(placeholder);
            }
            creator.into(holder.thumbnailView);
        }

        if (!mConsumedMap.get(i)) {
            mConsumedMap.put(i, true);
            // In which case we magically instantiate our effect and launch it directly on the view
            Animation animation = AnimationUtils.makeInChildBottomAnimation(getContext());
            rowView.startAnimation(animation);
        }

        return rowView;
    }

    static class ViewHolder {
        @BindView(android.R.id.text1)
        public TextView primaryTextView;
        @BindView(android.R.id.text2)
        public TextView secondaryTextView;
        @BindView(android.R.id.icon)
        public SquaredImageView thumbnailView;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}
