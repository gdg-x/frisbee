package org.gdg.frisbee.android.gde;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.fragment.BaseFragment;

/**
 * Created by maui on 03.06.2014.
 */
public class PlainLayoutFragment extends BaseFragment {

    private static final String ARG_LAYOUT_RES = "layout_res";

    public static PlainLayoutFragment newInstance(@LayoutRes int res) {
        PlainLayoutFragment frag = new PlainLayoutFragment();
        Bundle bndl = new Bundle();
        bndl.putInt(ARG_LAYOUT_RES, res);
        frag.setArguments(bndl);
        return frag;
    }

    private int mLayoutRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            mLayoutRes = bundle.containsKey(ARG_LAYOUT_RES) ? bundle.getInt(ARG_LAYOUT_RES) : R.layout.empty;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(mLayoutRes, container, false);
    }
}
