package org.gdg.frisbee.android.gde;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.common.PeopleListFragment;

import java.util.ArrayList;

public class GdeListFragment extends PeopleListFragment {

    private static final String ARG_PEOPLE_ARRAY = "people";

    public static GdeListFragment newInstance(@NonNull ArrayList<Gde> gdes) {
        GdeListFragment fragment = new GdeListFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(ARG_PEOPLE_ARRAY, gdes);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter.setPlaceholder(R.drawable.gde_dummy);

        ArrayList<Gde> gdeList = getArguments().getParcelableArrayList(ARG_PEOPLE_ARRAY);
        if (gdeList != null) {
            mAdapter.addAll(gdeList);
        }
    }
}
