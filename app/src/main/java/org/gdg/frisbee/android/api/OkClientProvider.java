package org.gdg.frisbee.android.api;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;

import retrofit.client.Client;
import retrofit.client.OkClient;

/**
 * Created by <a href="mailto:marcusandreog@gmail.com">Marcus Gabilheri</a>
 *
 * @author Marcus Gabilheri
 * @version 1.0
 * @since 7/26/15.
 */
public final class OkClientProvider {

    // Cache size for the OkHttpClient
    private static final int DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    private static OkHttpClient okHttpClient;
    private static OkClient okClient;

    private OkClientProvider() {
    }

    /**
     * The OkhttpClient is a fairly expensive object so we don't want to create instances
     * of this object all the time. That's why we keep this object as a Singleton keeping a reference
     * to the application context when initializing the cache directory
     *
     * @param context
     *         Context necessary to instantiate the cache directory
     * @return The OkhttpClient instance
     */
    static OkHttpClient getOkHttpClient(Context context) {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
            // Install an HTTP cache in the application cache directory.
            File cacheDir = new File(context.getApplicationContext().getCacheDir(), "http");
            Cache cache = new Cache(cacheDir, DISK_CACHE_SIZE);
            okHttpClient.setCache(cache);
        }
        return okHttpClient;
    }

    static Client getClient(Context context) {
        if (okClient == null) {
            okClient = new OkClient(getOkHttpClient(context));
        }
        return okClient;
    }
}
