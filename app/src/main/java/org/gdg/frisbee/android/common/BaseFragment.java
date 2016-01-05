package org.gdg.frisbee.android.common;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;

import com.squareup.leakcanary.RefWatcher;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.app.App;

import butterknife.ButterKnife;
import timber.log.Timber;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            RefWatcher refWatcher = App.getInstance().getRefWatcher();
            refWatcher.watch(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean isContextValid() {
        boolean isContextValid = getActivity() != null && !getActivity().isFinishing() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !getActivity().isDestroyed());
        if (!isContextValid) {
            Timber.d("Context is not valid");
        }
        return isContextValid;
    }
}
