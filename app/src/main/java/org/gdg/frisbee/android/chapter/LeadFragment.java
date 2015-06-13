package org.gdg.frisbee.android.chapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.lead.LeadMessage;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LeadFragment extends ListFragment {
    private LeadAnnouncementsAdapter mAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_for_leads, container, false);
    }

    public static Fragment newInstance(final String gplusId) {
        Fragment fragment = new LeadFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_PLUS_ID, gplusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new LeadAnnouncementsAdapter(getActivity());
        setListAdapter(mAdapter);
        mAdapter.add(LeadMessage.newMessage(getString(R.string.leads_welcome_title),
                getString(R.string.leads_welcome)));
        mAdapter.add(LeadMessage.newResource(getString(R.string.leads_resources_title),
                getString(R.string.leads_resources),
                Const.URL_GDG_RESOURCE_FOLDER));
        mAdapter.add(LeadMessage.newResource(getString(R.string.leads_wisdom_title),
                getString(R.string.leads_wisdom),
                Const.URL_GDG_WISDOM_BOOK));
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final LeadMessage item = mAdapter.getItem(position);
        if (item.getType() == LeadMessage.Type.resource) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLinkUrl())));
        }
    }

    private static class LeadAnnouncementsAdapter extends ArrayAdapter<LeadMessage> {
        public LeadAnnouncementsAdapter(final Context context) {
            super(context, R.layout.list_announcement_item);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_announcement_item, parent, false);
                final ViewHolder viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.title.setText(getItem(position).getTitle());
            viewHolder.details.setText(getItem(position).getDetails());
            viewHolder.type.setText(getContext().getString(getItem(position).getType().getName()));
            return convertView;
        }
    }

    public static class ViewHolder {
        @InjectView(R.id.msg_title)
        TextView title;
        @InjectView(R.id.msg_details)
        TextView details;
        @InjectView(R.id.msg_type)
        TextView type;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
