/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.cache;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.disklrucache.DiskLruCache;

import org.gdg.frisbee.android.api.deserializer.DateTimeDeserializer;
import org.gdg.frisbee.android.api.deserializer.DateTimeSerializer;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import timber.log.Timber;

public final class ModelCache {

    static final int DISK_CACHE_FLUSH_DELAY_SECS = 5;
    private Gson mGson;
    private LruCache<String, CacheItem> mMemoryCache;
    private DiskLruCache mDiskCache;
    // Variables which are only used when the Disk Cache is enabled
    private HashMap<String, ReentrantLock> mDiskCacheEditLocks;
    private ScheduledThreadPoolExecutor mDiskCacheFlusherExecutor;
    private DiskCacheFlushRunnable mDiskCacheFlusherRunnable;
    // Transient
    private ScheduledFuture<?> mDiskCacheFuture;

    ModelCache() {

        mGson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
            .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
            .create();
    }

    private static String transformUrlForDiskCacheKey(String url) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean contains(String url) {
        return containsInMemoryCache(url) || containsInDiskCache(url);
    }

    public boolean containsInDiskCache(String url) {
        if (null != mDiskCache) {
            checkNotOnMainThread();

            try {
                return null != mDiskCache.get(transformUrlForDiskCacheKey(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public boolean containsInMemoryCache(String url) {
        return null != mMemoryCache && null != mMemoryCache.get(url);
    }

    public void getAsync(final String url, final CacheListener mListener) {
        getAsync(url, true, mListener);
    }

    public void getAsync(final String url, final boolean checkExpiration, final CacheListener mListener) {
        new GetAsyncTask(ModelCache.this, url, checkExpiration, mListener).execute();
    }

    @Nullable
    public Object get(String url) {
        return get(url, true);
    }

    @Nullable
    public Object get(String url, boolean checkExpiration) {
        Timber.d("get(%s)", url);
        CacheItem result;

        // First try Memory Cache
        result = getFromMemoryCache(url, checkExpiration);

        if (null == result) {
            // Memory Cache failed, so try Disk Cache
            result = getFromDiskCache(url, checkExpiration);
        }

        if (result != null) {
            return result.getValue();
        } else {
            return null;
        }
    }

    @Nullable
    private CacheItem getFromDiskCache(final String url, boolean checkExpiration) {
        CacheItem result = null;

        if (mDiskCache == null) {
            return null;
        }
        checkNotOnMainThread();

        try {
            final String key = transformUrlForDiskCacheKey(url);
            DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
            if (null != snapshot) {

                Object value = readValueFromDisk(snapshot.getInputStream(0));
                DateTime expiresAt = new DateTime(readExpirationFromDisk(snapshot.getInputStream(1)));

                if (value != null) {

                    if (checkExpiration && expiresAt.isBeforeNow()) {
                        mDiskCache.remove(key);
                        scheduleDiskCacheFlush();
                    } else {
                        result = new CacheItem(value, expiresAt);
                        if (null != mMemoryCache) {
                            mMemoryCache.put(url, result);
                        }
                    }
                } else {
                    // If we get here, the file in the cache can't be
                    // decoded. Remove it and schedule a flush.
                    mDiskCache.remove(key);
                    scheduleDiskCacheFlush();
                }
            }
        } catch (Exception e) {
            Timber.e(e, "getFromDiskCache failed for key: %s", url);
        }

        return result;
    }

    private CacheItem getFromMemoryCache(final String url, boolean checkExpiration) {
        CacheItem result = null;

        if (null != mMemoryCache) {
            synchronized (mMemoryCache) {
                result = mMemoryCache.get(url);

                // If we get a value, that has expired
                if (null != result && result.getExpiresAt().isBeforeNow() && checkExpiration) {
                    mMemoryCache.remove(url);
                    result = null;
                }
            }
        }

        return result;
    }

    public boolean isDiskCacheEnabled() {
        return null != mDiskCache;
    }

    public boolean isMemoryCacheEnabled() {
        return null != mMemoryCache;
    }

    public void putAsync(final String url, final Object obj,
                         final DateTime expiresAt, final CachePutListener onDoneListener) {
        new PutAsyncTask(ModelCache.this, url, obj, expiresAt, onDoneListener).execute();
    }

    CacheItem put(final String url, final Object obj, DateTime expiresAt) {

        if (obj == null) {
            return null;
        }

        Timber.d("put(%s)", url);
        CacheItem d = new CacheItem(obj, expiresAt);

        if (null != mMemoryCache) {
            mMemoryCache.put(url, d);
        }

        if (null != mDiskCache) {
            checkNotOnMainThread();

            final String key = transformUrlForDiskCacheKey(url);
            final ReentrantLock lock = getLockForDiskCacheEdit(key);
            lock.lock();
            try {
                DiskLruCache.Editor editor = mDiskCache.edit(key);
                writeValueToDisk(editor.newOutputStream(0), obj);
                writeExpirationToDisk(editor.newOutputStream(1), expiresAt);
                editor.commit();
            } catch (Exception e) {
                Timber.e(e, "Error while putting %s with key:%s to ModelCache", obj.toString(), url);
            } finally {
                lock.unlock();
                scheduleDiskCacheFlush();
            }
        }

        return d;
    }

    public void removeAsync(final String url) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                remove(url);
                return null;
            }
        }.execute();
    }

    void remove(String url) {
        if (null != mMemoryCache) {
            mMemoryCache.remove(url);
        }

        if (null != mDiskCache) {
            checkNotOnMainThread();

            try {
                mDiskCache.remove(transformUrlForDiskCacheKey(url));
                scheduleDiskCacheFlush();
            } catch (Exception e) {
                Timber.e(e, "Error while removing key: %s", url);
            }
        }
    }

    synchronized void setDiskCache(DiskLruCache diskCache) {
        mDiskCache = diskCache;

        if (null != diskCache) {
            mDiskCacheEditLocks = new HashMap<>();
            mDiskCacheFlusherExecutor = new ScheduledThreadPoolExecutor(1);
            mDiskCacheFlusherRunnable = new DiskCacheFlushRunnable(diskCache);
        }
    }

    void setMemoryCache(LruCache<String, CacheItem> memoryCache) {
        mMemoryCache = memoryCache;
    }

    private ReentrantLock getLockForDiskCacheEdit(String url) {
        synchronized (mDiskCacheEditLocks) {
            ReentrantLock lock = mDiskCacheEditLocks.get(url);
            if (null == lock) {
                lock = new ReentrantLock();
                mDiskCacheEditLocks.put(url, lock);
            }
            return lock;
        }
    }

    private void scheduleDiskCacheFlush() {
        // If we already have a flush scheduled, cancel it
        if (null != mDiskCacheFuture) {
            mDiskCacheFuture.cancel(false);
        }

        // Schedule a flush
        mDiskCacheFuture = mDiskCacheFlusherExecutor
            .schedule(mDiskCacheFlusherRunnable, DISK_CACHE_FLUSH_DELAY_SECS,
                TimeUnit.SECONDS);
    }

    private void writeExpirationToDisk(OutputStream os, DateTime expiresAt) throws IOException {

        DataOutputStream out = new DataOutputStream(os);

        out.writeLong(expiresAt.getMillis());

        out.close();
    }

    private void writeValueToDisk(OutputStream os, Object o) throws IOException {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));

        String className = o.getClass().getCanonicalName();

        out.write(className + "\n");

        String json = mGson.toJson(o);
        out.write(json);

        out.close();
    }

    private long readExpirationFromDisk(InputStream is) throws IOException {
        DataInputStream din = new DataInputStream(is);
        long expiration = din.readLong();
        din.close();
        return expiration;
    }

    private Object readValueFromDisk(InputStream is) throws IOException, ClassNotFoundException {
        BufferedReader fss = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String className = fss.readLine();

        if (className == null) {
            return null;
        }

        String line;
        String content = "";
        while ((line = fss.readLine()) != null) {
            content += line;
        }

        fss.close();

        Class<?> clazz = Class.forName(className);
        return mGson.fromJson(content, clazz);
    }

    public interface CachePutListener {

        void onPutIntoCache();

    }

    public interface CacheListener {

        void onGet(Object item);

        void onNotFound(String key);

    }

    public static class Builder {

        static final int MEGABYTE = 1024 * 1024;

        static final int DEFAULT_DISK_CACHE_MAX_SIZE_MB = 10;

        private boolean mDiskCacheEnabled;
        private File mDiskCacheLocation;
        private long mDiskCacheMaxSize;
        private boolean mMemoryCacheEnabled;
        private int mMemoryCacheMaxSize;

        public Builder(Context context) {
            // Disk Cache is disabled by default, but it's default size is set
            mDiskCacheMaxSize = DEFAULT_DISK_CACHE_MAX_SIZE_MB * MEGABYTE;

            // Memory Cache is enabled by default, with a small maximum size
            mMemoryCacheEnabled = true;
            mMemoryCacheMaxSize = calculateMemoryCacheSize(context);
        }

        static int calculateMemoryCacheSize(Context context) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            int memoryClass = am.getMemoryClass();
            // Target ~6% of the available heap.
            return MEGABYTE * memoryClass / 16;
        }

        private static long getHeapSize() {
            return Runtime.getRuntime().maxMemory();
        }

        public ModelCache build() {
            final ModelCache cache = new ModelCache();

            if (isValidOptionsForMemoryCache()) {
                cache.setMemoryCache(new LruCache<String, CacheItem>(mMemoryCacheMaxSize));
            }

            if (isValidOptionsForDiskCache()) {
                new AsyncTask<Void, Void, DiskLruCache>() {

                    @Override
                    protected DiskLruCache doInBackground(Void... params) {
                        try {
                            DiskLruCache c = DiskLruCache.open(mDiskCacheLocation, 0, 2, mDiskCacheMaxSize);
                            cache.setDiskCache(c);
                            return c;
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(DiskLruCache result) {
                        //cache.setDiskCache(result);
                    }

                }.execute();
            }

            return cache;
        }

        /**
         * Set whether the Disk Cache should be enabled. Defaults to {@code false}.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setDiskCacheEnabled(boolean enabled) {
            mDiskCacheEnabled = enabled;
            return this;
        }

        /**
         * Set the Disk Cache location. This location should be read-writeable.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setDiskCacheLocation(File location) {
            mDiskCacheLocation = location;
            return this;
        }

        /**
         * Set the maximum number of bytes the Disk Cache should use to store values. Defaults to
         * {@value #DEFAULT_DISK_CACHE_MAX_SIZE_MB}MB.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setDiskCacheMaxSize(long maxSize) {
            mDiskCacheMaxSize = maxSize;
            return this;
        }

        /**
         * Set whether the Memory Cache should be enabled. Defaults to {@code true}.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setMemoryCacheEnabled(boolean enabled) {
            mMemoryCacheEnabled = enabled;
            return this;
        }

        /**
         * Set the maximum number of bytes the Memory Cache should use to store values.
         * Defaults to 1/16 of the available memory.
         *
         * @return This Builder object to allow for chaining of calls to set methods.
         */
        public Builder setMemoryCacheMaxSize(int size) {
            mMemoryCacheMaxSize = size;
            return this;
        }

        private boolean isValidOptionsForDiskCache() {
            if (mDiskCacheEnabled) {
                if (null == mDiskCacheLocation) {
                    return false;
                } else if (!mDiskCacheLocation.canWrite()) {
                    Timber.i("Disk Cache Location is not write-able, disabling disk caching.");
                    return false;
                }

                return true;
            }
            return false;
        }

        private boolean isValidOptionsForMemoryCache() {
            return mMemoryCacheEnabled && mMemoryCacheMaxSize > 0;
        }

    }

    static final class DiskCacheFlushRunnable implements Runnable {

        private final DiskLruCache mDiskCache;

        public DiskCacheFlushRunnable(DiskLruCache cache) {
            mDiskCache = cache;
        }

        public void run() {
            // Make sure we're running with a background priority
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            try {
                mDiskCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static class GetAsyncTask extends AsyncTask<Void, Void, Object> {

        private ModelCache modelCache;

        private CacheListener listener;
        private String key;
        private boolean checkExpiration;

        public GetAsyncTask(ModelCache modelCache, String key, boolean checkExpiration, CacheListener listener) {
            this.modelCache = modelCache;
            this.key = key;
            this.checkExpiration = checkExpiration;
            this.listener = listener;
        }

        @Override
        protected Object doInBackground(Void... voids) {
            if (modelCache != null) {
                return modelCache.get(key, checkExpiration);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (listener == null) {
                return;
            }

            if (o != null) {
                listener.onGet(o);
            } else {
                listener.onNotFound(key);
            }
        }
    }

    public static class PutAsyncTask extends AsyncTask<Void, Void, Object> {

        private ModelCache modelCache;

        private CachePutListener onDoneListener;
        private String key;
        private Object obj;
        private DateTime expiresAt;

        public PutAsyncTask(ModelCache modelCache, String key, Object obj,
                            DateTime expiresAt, CachePutListener onDoneListener) {
            this.modelCache = modelCache;
            this.key = key;
            this.obj = obj;
            this.expiresAt = expiresAt;
            this.onDoneListener = onDoneListener;
        }

        @Override
        protected Object doInBackground(Void... voids) {
            if (modelCache != null) {
                return modelCache.put(key, obj, expiresAt);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            if (onDoneListener != null) {
                onDoneListener.onPutIntoCache();
            }
        }

    }

    public class CacheItem {

        private Object mValue;

        private DateTime mExpiresAt;

        public CacheItem(Object value, DateTime expiresAt) {
            mValue = value;
            mExpiresAt = expiresAt;
        }

        public DateTime getExpiresAt() {
            return mExpiresAt;
        }

        public void setExpiresAt(DateTime mExpiresAt) {
            this.mExpiresAt = mExpiresAt;
        }

        public Object getValue() {
            return mValue;
        }

        public void setValue(Object mValue) {
            this.mValue = mValue;
        }

    }

    private static void checkNotOnMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException(
                "This method should not be called from the main/UI thread.");
        }
    }
}
