package org.gdg.frisbee.android.fragment;

import android.support.v4.app.Fragment;

import com.squareup.leakcanary.RefWatcher;

import org.gdg.frisbee.android.app.App;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = App.getInstance().getRefWatcher();
        refWatcher.watch(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
