package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.BuildConfig;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyAdder implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        HttpUrl newUrl = original.url().newBuilder()
            .addQueryParameter("key", BuildConfig.IP_SIMPLE_API_ACCESS_KEY).build();
        Request.Builder requestBuilder = original.newBuilder().url(newUrl);

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
