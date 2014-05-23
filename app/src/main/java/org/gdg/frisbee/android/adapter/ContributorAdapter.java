package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.app.App;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 08.07.13
 * Time: 02:28
 * To change this template use File | Settings | File Templates.
 */
public class ContributorAdapter extends ArrayAdapter<Contributor> {

    private Context mContext;
    private LayoutInflater mInflater;

    public ContributorAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null)
            view = mInflater.inflate(R.layout.contributor_item, null);

        Contributor contributor = getItem(position);

        TextView name = (TextView)view.findViewById(R.id.contributorName);
        name.setText(contributor.getLogin());
        final ImageView c = (ImageView) view.findViewById(R.id.contributorIcon);
        App.getInstance().getPicasso()
                .load(contributor.getAvatarUrl())
                .into(c);

        return view;
    }

    @Override
    public void addAll(Collection<? extends Contributor> contributors) {
        for (Contributor c : contributors) {
            add(c);
        }
    }
}
