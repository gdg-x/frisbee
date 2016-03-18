package org.gdg.frisbee.android.common;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.squareup.leakcanary.RefWatcher;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.view.ColoredSnackBar;

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
        boolean isContextValid = getActivity() != null
            && !getActivity().isFinishing()
            && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !getActivity().isDestroyed());
        if (!isContextValid) {
            Timber.d("Context is not valid");
        }
        return isContextValid;
    }

    protected void showError(@StringRes final int errorStringRes) {
        if (isContextValid()) {
            if (getView() != null) {
                Snackbar snackbar = Snackbar.make(getView(), errorStringRes,
                    Snackbar.LENGTH_SHORT);
                ColoredSnackBar.alert(snackbar).show();
            } else {
                Toast.makeText(getActivity(), errorStringRes, Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void showError(String errorString) {
        if (isContextValid()) {
            if (getView() != null) {
                Snackbar snackbar = Snackbar.make(getView(), errorString,
                    Snackbar.LENGTH_SHORT);
                ColoredSnackBar.alert(snackbar).show();
            } else {
                Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
