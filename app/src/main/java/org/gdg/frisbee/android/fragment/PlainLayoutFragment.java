package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.gdg.frisbee.android.R;

/**
 * Created by maui on 03.06.2014.
 */
public class PlainLayoutFragment extends Fragment {

    public static PlainLayoutFragment newInstance(int res) {
        PlainLayoutFragment frag = new PlainLayoutFragment();
        Bundle bndl = new Bundle();
        bndl.putInt("layout_res", res);
        frag.setArguments(bndl);
        return frag;
    }

    private int mLayoutRes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null) {
            Bundle bndl = getArguments();

            mLayoutRes = bndl.containsKey("layout_res") ? bndl.getInt("layout_res") : R.layout.empty;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(mLayoutRes, container, false);
    }
}
