package org.gdg.frisbee.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdeActivity;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.adapter.GdeAdapter;
import org.gdg.frisbee.android.api.model.Gde;
import timber.log.Timber;

import java.util.ArrayList;

/**
 * Created by maui on 29.05.2014.
 */
public class GdeListFragment extends GdgListFragment implements GdeActivity.Listener {

    private static final String LOG_TAG = "GDG-GdeListFragment";

    @InjectView(android.R.id.list)
    GridView mGrid;

    private GdeAdapter mAdapter;

    public static GdeListFragment newInstance(ArrayList<Gde> gdes, boolean active) {
        GdeListFragment fragment = new GdeListFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("gdes", gdes);
        arguments.putBoolean("active", active);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("onCreateView()");
        View v = inflater.inflate(R.layout.fragment_gde_list, null);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.d("onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated()");

        mAdapter = new GdeAdapter(getActivity(), ((GdgActivity)getActivity()).getGoogleApiClient());

        if(getArguments().containsKey("gdes")) {
            ArrayList<Gde> gdes = getArguments().getParcelableArrayList("gdes");
            mAdapter.addAll(gdes);
        }

        setListAdapter(mAdapter);

        if(getArguments().getBoolean("active")) {
            onPageSelected();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Gde gde = (Gde) mAdapter.getItem(position);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(gde.getSocialUrl())));

    }

    @Override
    public void onPageSelected() {
        //((GdgActivity)getActivity()).getPullToRefreshHelper().addRefreshableView(getListView(), this);
    }

}
