package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.widget.GridView;
import butterknife.InjectView;
import org.gdg.frisbee.android.api.model.Gde;

import java.util.ArrayList;

/**
 * Created by maui on 29.05.2014.
 */
public class GdeListFragment extends GdgListFragment {

    private static final String LOG_TAG = "GDG-GdeListFragment";

    @InjectView(android.R.id.list)
    GridView mGrid;

    public static GdeListFragment newInstance(ArrayList<Gde> gdes, boolean active) {
        GdeListFragment fragment = new GdeListFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList("gdes", gdes);
        arguments.putBoolean("active", active);
        fragment.setArguments(arguments);
        return fragment;
    }
}
