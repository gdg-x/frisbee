package org.gdg.frisbee.android.gde;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.common.GdgListFragment;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * Created by maui on 29.05.2014.
 */
public class GdeListFragment extends GdgListFragment {

    private static final String ARG_GDES = "gdes";
    private static final String ARG_ACTIVE = "active";

    private GdeAdapter mAdapter;

    public static GdeListFragment newInstance(@NonNull ArrayList<Gde> gdes, boolean active) {
        GdeListFragment fragment = new GdeListFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(ARG_GDES, gdes);
        arguments.putBoolean(ARG_ACTIVE, active);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gde_list, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new GdeAdapter(getActivity(), ((GdgActivity) getActivity()).getGoogleApiClient());

        if (getArguments().containsKey(ARG_GDES)) {
            ArrayList<Gde> gdeList = getArguments().getParcelableArrayList(ARG_GDES);
            mAdapter.addAll(gdeList);
        }

        setListAdapter(mAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Gde gde = (Gde) mAdapter.getItem(position);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(gde.getSocialUrl())));

    }
}
