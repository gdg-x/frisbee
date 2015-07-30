package org.gdg.frisbee.android.gde;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.common.PeopleAdapter;
import org.gdg.frisbee.android.common.PeopleListFragment;

import java.util.ArrayList;

/**
 * Created by tasomaniac on 27/7/15.
 */
public class GdeListFragment extends PeopleListFragment {

    private static final String ARG_PEOPLE_ARRAY = "people";

    public static PeopleListFragment newInstance(@NonNull ArrayList<Gde> gdes) {
        PeopleListFragment fragment = new PeopleListFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(ARG_PEOPLE_ARRAY, gdes);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new PeopleAdapter(getActivity(), R.drawable.gde_dummy);

        ArrayList<Gde> gdeList = getArguments().getParcelableArrayList(ARG_PEOPLE_ARRAY);
        if (gdeList != null) {
            mAdapter.addAll(gdeList);
        }
    }
}
