package org.gdg.frisbee.android.api;

import android.support.annotation.StringRes;

import org.gdg.frisbee.android.R;

import java.io.IOException;

import retrofit2.Response;
import timber.log.Timber;

public abstract class Callback<T> implements retrofit2.Callback<T> {

    @Override
    public final void onResponse(Response<T> response) {
        if (response.isSuccess()) {
            success(response.body());
        } else {
            try {
                final Exception e = new Exception(response.errorBody().string());
                Timber.e(e, "Network Error!");
                failure(e, R.string.server_error);
            } catch (IOException e) {
                Timber.e(e, "Network Error!");
                failure(e, R.string.server_error);
            }
        }
    }

    @Override
    public final void onFailure(Throwable t) {
        Timber.d(t, "Network Failure!");
        failure(t, R.string.offline_alert);
    }

    public void failure(Throwable t, @StringRes int errorMessage) {
    }

    public abstract void success(T response);

}
