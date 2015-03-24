package org.gdg.frisbee.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;

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
        mAdapter.add(getString(R.string.leads_welcome));
        mAdapter.add(getString(R.string.leads_resources));
        mAdapter.add(getString(R.string.leads_wisdom));
    }


    private static class LeadAnouncementAdapter extends ArrayAdapter<String> {
        public LeadAnouncementAdapter(final Context context) {
            super(context, R.layout.list_announcement_item);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_announcement_item, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.title = (TextView) convertView.findViewById(R.id.event_title);
                convertView.setTag(viewHolder);
            }

            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.title.setText(getItem(position));

            return convertView;
        }

        private static class ViewHolder {
            TextView title;
        }
    }
}
