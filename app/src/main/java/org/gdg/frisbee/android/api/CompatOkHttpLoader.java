package org.gdg.frisbee.android.api;

import android.content.Context;
import android.net.Uri;
import android.os.StatFs;
import com.squareup.okhttp.HttpResponseCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Loader;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 01.07.13
 * Time: 03:31
 * To change this template use File | Settings | File Templates.
 */
public class CompatOkHttpLoader implements Loader {

    static final String RESPONSE_SOURCE = "X-Android-Response-Source";

    private final OkHttpClient client;

    /**
     * Create new loader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     */
    public CompatOkHttpLoader(final Context context) {
        this(createDefaultCacheDir(context));
    }

    /**
     * Create new loader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     *
     * @param cacheDir The directory in which the cache should be stored
     */
    public CompatOkHttpLoader(final File cacheDir) {
        this(cacheDir, calculateDiskCacheSize(cacheDir));
    }

    /**
     * Create new loader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     *
     * @param maxSize The size limit for the cache.
     */
    public CompatOkHttpLoader(final Context context, final int maxSize) {
        this(createDefaultCacheDir(context), maxSize);
    }

    /**
     * Create new loader that uses OkHttp. This will install an image cache into your application
     * cache directory.
     *
     * @param cacheDir The directory in which the cache should be stored
     * @param maxSize The size limit for the cache.
     */
    public CompatOkHttpLoader(final File cacheDir, final int maxSize) {
        this();
        try {
            client.setResponseCache(new HttpResponseCache(cacheDir, maxSize));
        } catch (IOException ignored) {
        }
    }

    /**
     * Create a new loader that uses the specified OkHttp instance. A response cache will not be
     * automatically configured.
     */
    public CompatOkHttpLoader() {
        client = new OkHttpClient();
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
        client.setSslSocketFactory(sslContext.getSocketFactory());
    }

    @Override public Response load(Uri uri, boolean localCacheOnly) throws IOException {
        HttpURLConnection connection = client.open(new URL(uri.toString()));
        connection.setUseCaches(true);
        if (localCacheOnly) {
            connection.setRequestProperty("Cache-Control", "only-if-cached");
        }

        boolean fromCache = parseResponseSourceHeader(connection.getHeaderField(RESPONSE_SOURCE));

        return new Response(connection.getInputStream(), fromCache);
    }

    static boolean parseResponseSourceHeader(String header) {
        if (header == null) {
            return false;
        }
        String[] parts = header.split(" ", 2);
        if ("CACHE".equals(parts[0])) {
            return true;
        }
        if (parts.length == 1) {
            return false;
        }
        try {
            return "CONDITIONAL_CACHE".equals(parts[0]) && Integer.parseInt(parts[1]) == 304;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static final String PICASSO_CACHE = "picasso-cache";
    private static final int MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024; // 50MB

    static File createDefaultCacheDir(Context context) {
        File cache = new File(context.getApplicationContext().getCacheDir(), PICASSO_CACHE);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    static int calculateDiskCacheSize(File dir) {
        StatFs statFs = new StatFs(dir.getAbsolutePath());
        int available = statFs.getBlockCount() * statFs.getBlockSize();
        // Target 2% of the total space.
        int size = available / 50;
        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_CACHE_SIZE), MIN_DISK_CACHE_SIZE);
    }
}
