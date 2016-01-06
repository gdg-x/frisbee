package org.gdg.frisbee.android.api;

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
                failure(e);
            } catch (IOException e) {
                Timber.e(e, "Network Error!");
                failure(e);
            }
        }
    }

    @Override
    public final void onFailure(Throwable t) {
        Timber.d(t, "Network Failure!");
        networkFailure(t);
    }

    public void failure(Throwable error) {
    }

    public void networkFailure(Throwable error) {
    }

    public abstract void success(T response);

}
