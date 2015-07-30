package org.gdg.frisbee.android.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.GdgPerson;

import butterknife.ButterKnife;

public class PeopleListFragment extends GdgListFragment {

    protected PeopleAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_people_list, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new PeopleAdapter(getActivity());
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        GdgPerson person = mAdapter.getItem(position);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(person.getUrl())));
    }
}
