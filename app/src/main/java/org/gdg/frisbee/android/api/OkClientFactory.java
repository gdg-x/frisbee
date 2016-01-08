package org.gdg.frisbee.android.api;

import android.app.Application;
import android.support.annotation.NonNull;

import org.gdg.frisbee.android.BuildConfig;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

public final class OkClientFactory {

    // Cache size for the OkHttpClient
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    private OkClientFactory() {
    }

    @NonNull
    public static OkHttpClient provideOkHttpClient(Application app) {
        // Install an HTTP cache in the application cache directory.
        File cacheDir = new File(app.getCacheDir(), "http");
        Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);

        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder()
                .cache(cache);
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Timber.tag("OkHttp").v(message);
                }
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpClient.addInterceptor(loggingInterceptor);
        }
        return okHttpClient.build();
    }

    public static OkHttpClient okHttpClientWithIdlingResources(OkHttpClient client) {
        return client.newBuilder()
                .addInterceptor(OkClientFactory.provideIdlingResourcesInterceptor())
                .build();
    }

    private static Interceptor provideIdlingResourcesInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                EspressoIdlingResource.increment();

                Request request = chain.request();
                return chain.proceed(request);
            }
        };
    }
}
