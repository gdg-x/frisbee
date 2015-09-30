package org.gdg.frisbee.android.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.gdg.frisbee.android.BuildConfig;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

public final class OkClientFactory {

    // Cache size for the OkHttpClient
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    private OkClientFactory() {
    }

    @NonNull
    public static OkHttpClient provideOkHttpClient(Context context) {
        OkHttpClient okHttpClient = new OkHttpClient();
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(context.getApplicationContext().getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
        okHttpClient.setCache(cache);
        if (BuildConfig.DEBUG) {
            okHttpClient.interceptors().add(new LoggingInterceptor());
        }
        return okHttpClient;
    }

    private static class LoggingInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Timber.i(String.format("Sending request %s on %s",
                    request.url(), chain.connection()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Timber.i(String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }
}
