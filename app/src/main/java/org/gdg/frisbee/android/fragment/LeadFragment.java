package org.gdg.frisbee.android.fragment;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.lead.LeadMessage;

public class LeadFragment extends ListFragment {
    private LeadAnouncementAdapter mAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_for_leads, null);
        return root;
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
        mAdapter = new LeadAnouncementAdapter(getActivity());
        setListAdapter(mAdapter);
        mAdapter.add(LeadMessage.newMessage(getString(R.string.leads_welcome_title),
                getString(R.string.leads_welcome)));
        mAdapter.add(LeadMessage.newResource(getString(R.string.leads_resources_title),
                getString(R.string.leads_resources),
                "https://drive.google.com/drive/#folders/0B55wxScz_BJtWW9aUnk2LUlNdEk"));
        mAdapter.add(LeadMessage.newResource(getString(R.string.leads_wisdom_title),
                getString(R.string.leads_wisdom),
                "http://gdg-wisdom.gitbooks.io/gdg-wisdom-2015/content/"));
    }

    @Override
    public void onListItemClick(final ListView l, final View v, final int position, final long id) {
        final LeadMessage item = mAdapter.getItem(position);
        if (item.getType() == LeadMessage.Type.resource) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLinkUrl())));
        }
    }

    private static class LeadAnouncementAdapter extends ArrayAdapter<LeadMessage> {
        public LeadAnouncementAdapter(final Context context) {
            super(context, R.layout.list_announcement_item);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_announcement_item, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.msg_title);
                viewHolder.details = (TextView) convertView.findViewById(R.id.msg_details);
                viewHolder.icon = (ImageView) convertView.findViewById(R.id.msg_icon);
                viewHolder.type = (TextView) convertView.findViewById(R.id.msg_type);
                convertView.setTag(viewHolder);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.title.setText(getItem(position).getTitle());
            viewHolder.details.setText(getItem(position).getDetails());

            return convertView;
        }

        private static class ViewHolder {
            TextView title;
            TextView details;
            TextView type;
            ImageView icon;
        }
    }
}
