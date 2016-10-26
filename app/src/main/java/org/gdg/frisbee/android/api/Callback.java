package org.gdg.frisbee.android.api;

import java.io.IOException;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public abstract class Callback<T> implements retrofit2.Callback<T> {

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onSuccess(response.body());
        } else {
            try {
                String errorLog = String.format(Locale.getDefault(),
                    "Network call to %s failed with body: %s",
                    call.request().url().toString(),
                    response.errorBody().string());
                Timber.e(new RuntimeException(errorLog));
                onError();
            } catch (IOException e) {
                Timber.e(e, "Error while parsing error body.");
                onError();
            }
        }
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        Timber.d(t, "Network failure!");
        onNetworkFailure(t);
    }

    public void onError() {
    }

    public void onNetworkFailure(Throwable error) {
    }

    public abstract void onSuccess(T response);

}
