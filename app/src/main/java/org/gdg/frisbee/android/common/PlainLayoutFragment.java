package org.gdg.frisbee.android.common;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.gdg.frisbee.android.R;

public class PlainLayoutFragment extends BaseFragment {

    private static final String ARG_LAYOUT_RES = "layout_res";
    private int mLayoutRes;

    public static PlainLayoutFragment newInstance(@LayoutRes int res) {
        PlainLayoutFragment frag = new PlainLayoutFragment();
        Bundle bndl = new Bundle();
        bndl.putInt(ARG_LAYOUT_RES, res);
        frag.setArguments(bndl);
        return frag;
    }

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
