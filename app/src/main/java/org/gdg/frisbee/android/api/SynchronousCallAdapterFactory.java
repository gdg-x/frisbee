package org.gdg.frisbee.android.api;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

public class SynchronousCallAdapterFactory extends CallAdapter.Factory {
    public static CallAdapter.Factory create() {
        return new SynchronousCallAdapterFactory();
    }

    @Override
    public CallAdapter<Object> get(final Type returnType, Annotation[] annotations, Retrofit retrofit) {
        // if returnType is retrofit2.Call, do nothing
        if (returnType.getClass().getName().contains("retrofit2.Call")) {
            return null;
        }

        return new CallAdapter<Object>() {
            @Override
            public Type responseType() {
                return returnType;
            }

            @Override
            public <R> Object adapt(Call<R> call) {
                try {
                    Response<R> response = call.execute();
                    if (response.isSuccessful()) {
                        return response.body();
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    Timber.e(e, "while executing retrofit call: " + call);
                    return null;
                }
            }
        };
    }
}
