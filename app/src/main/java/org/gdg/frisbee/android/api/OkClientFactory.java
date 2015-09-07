package org.gdg.frisbee.android.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;

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
        return okHttpClient;
    }
}
