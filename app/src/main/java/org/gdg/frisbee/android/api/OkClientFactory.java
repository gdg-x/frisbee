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
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okhttp3.logging.HttpLoggingInterceptor.Logger;
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

        okHttpClient.addInterceptor(provideIdlingResourcesInterceptor());

        if (BuildConfig.DEBUG) {
            okHttpClient.addInterceptor(provideHttpLoggingInterceptor());
        }
        return okHttpClient.build();
    }

    private static HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new Logger() {
            @Override
            public void log(String message) {
                Timber.tag("OkHttp").v(message);
            }
        });
        loggingInterceptor.setLevel(Level.BODY);
        return loggingInterceptor;
    }

    private static Interceptor provideIdlingResourcesInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                EspressoIdlingResource.increment();

                Request request = chain.request();
                Response proceed = chain.proceed(request);

                EspressoIdlingResource.decrement();
                return proceed;
            }
        };
    }
}
