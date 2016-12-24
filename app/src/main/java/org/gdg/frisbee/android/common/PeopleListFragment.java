package org.gdg.frisbee.android.common;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.GdgPerson;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;

public class PeopleListFragment extends GdgListFragment {

    protected PeopleAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, R.layout.fragment_people_list, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new PeopleAdapter(getActivity(), App.from(getContext()).getPicasso());
        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        GdgPerson person = mAdapter.getItem(position);
        startActivity(Utils.createExternalIntent(getActivity(), Uri.parse(person.getUrl())));
    }
}
