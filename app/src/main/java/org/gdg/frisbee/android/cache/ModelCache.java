/*
 * Copyright 2013 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.cache;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.util.LruCache;
import android.util.Log;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import android.os.Process;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jakewharton.disklrucache.DiskLruCache;
import org.gdg.frisbee.android.api.deserializer.DateTimeDeserializer;
import org.gdg.frisbee.android.api.deserializer.DateTimeSerializer;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;
import timber.log.Timber;

import java.io.*;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.cache
 * <p/>
 * User: maui
 * Date: 27.05.13
 * Time: 23:51
 */
public class ModelCache {

    public static class Builder {

        static final int MEGABYTE = 1024 * 1024;

        static final int DEFAULT_MEM_CACHE_MAX_SIZE = 32;

        static final int DEFAULT_DISK_CACHE_MAX_SIZE_MB = 10;

        private static long getHeapSize() {
            return Runtime.getRuntime().maxMemory();
        }

        private Context mContext;

        private boolean mDiskCacheEnabled;

        private File mDiskCacheLocation;

        private long mDiskCacheMaxSize;

        private boolean mMemoryCacheEnabled;

        private int mMemoryCacheMaxSize;

        public Builder() {
            this(null);
        }

        public Builder(Context context) {
            mContext = context;

            // Disk Cache is disabled by default, but it's default size is set
            mDiskCacheMaxSize = DEFAULT_DISK_CACHE_MAX_SIZE_MB * MEGABYTE;

            // Memory Cache is enabled by default, with a small maximum size
            mMemoryCacheEnabled = true;
            mMemoryCacheMaxSize = DEFAULT_MEM_CACHE_MAX_SIZE;
        }

        public ModelCache build() {
            final ModelCache cache = new ModelCache(mContext);

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
         * Set the maximum number of bytes the Memory Cache should use to store values. Defaults to
         * {@value #DEFAULT_MEM_CACHE_MAX_SIZE}MB.
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

    static final int DISK_CACHE_FLUSH_DELAY_SECS = 5;

    private File mTempDir;
    private Resources mResources;

    private static final String LOG_TAG = "GDG-ModelCache";

    final JsonFactory mJsonFactory = new GsonFactory();

    private Gson mGson;

    private LruCache<String, CacheItem> mMemoryCache;

    private DiskLruCache mDiskCache;

    // Variables which are only used when the Disk Cache is enabled
    private HashMap<String, ReentrantLock> mDiskCacheEditLocks;

    private ScheduledThreadPoolExecutor mDiskCacheFlusherExecutor;

    private DiskCacheFlushRunnable mDiskCacheFlusherRunnable;

    // Transient
    private ScheduledFuture<?> mDiskCacheFuture;

    ModelCache(Context context) {
        if (context != null) {
            // Make sure we have the application context
            context = context.getApplicationContext();

            mTempDir = context.getCacheDir();
            mResources = context.getResources();
        }

        mGson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
                .create();
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
        new AsyncTask<Void,Void,Object>() {

            @Override
            protected Object doInBackground(Void... voids) {
                return ModelCache.this.get(url, checkExpiration);
            }

            @Override
            protected void onPostExecute(Object o) {
                if(o != null)
                    mListener.onGet(o);
                else
                    mListener.onNotFound(url);
            }
        }.execute();
    }

    public void getAsync(final String url, final boolean checkExpiration, final boolean forceFetch, final CacheListener mListener) {
        new AsyncTask<Void,Void,Object>() {

            @Override
            protected Object doInBackground(Void... voids) {
                if(!forceFetch)
                    return ModelCache.this.get(url, checkExpiration);
                else
                    return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if(o != null)
                    mListener.onGet(o);
                else
                    mListener.onNotFound(url);
            }
        }.execute();
    }

    public Object get(String url) {
        return get(url, true);
    }

    public Object get(String url, boolean checkExpiration) {
        Timber.d(String.format("get(%s)", url));
        CacheItem result;

        // First try Memory Cache
        result = getFromMemoryCache(url, checkExpiration);

        if (null == result) {
            // Memory Cache failed, so try Disk Cache
            result = getFromDiskCache(url, checkExpiration);
        }

        if(result != null)
            return result.getValue();
        else
            return null;
    }

    public CacheItem getFromDiskCache(final String url, boolean checkExpiration) {
        CacheItem result = null;

        if (null != mDiskCache) {
            checkNotOnMainThread();

            try {
                final String key = transformUrlForDiskCacheKey(url);
                DiskLruCache.Snapshot snapshot = mDiskCache.get(key);
                if (null != snapshot) {

                    Object value = readValueFromDisk(snapshot.getInputStream(0));
                    DateTime expiresAt = new DateTime(readExpirationFromDisk(snapshot.getInputStream(1)));

                    if (value != null && expiresAt != null) {

                        if(checkExpiration && expiresAt.isBeforeNow()) {
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
            } catch (IOException e) {
                Timber.e("getFromDiskCache failed.", e);
                e.printStackTrace();
            }
        }

        return result;
    }

    public CacheItem getFromMemoryCache(final String url, boolean checkExpiration) {
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

    public CacheItem put(final String url, final Object obj) {
        return put(url, obj, new DateTime(0));
    }

    public void putAsync(final String url, final Object obj, final CachePutListener onDoneListener) {
        putAsync(url, obj, new DateTime(0), onDoneListener);
    }

    public void putAsync(final String url, final Object obj, final DateTime expiresAt, final CachePutListener onDoneListener) {
        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                put(url, obj, expiresAt);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(onDoneListener != null) {
                    onDoneListener.onPutIntoCache();
                }
            }
        }.execute();
    }

    public CacheItem put(final String url, final Object obj, DateTime expiresAt) {

        if(obj == null)
            return null;

        Timber.d(String.format("put(%s)", url));
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
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
                scheduleDiskCacheFlush();
            }
        }

        return d;
    }

    public void remove(String url) {
        if (null != mMemoryCache) {
            mMemoryCache.remove(url);
        }

        if (null != mDiskCache) {
            checkNotOnMainThread();

            try {
                mDiskCache.remove(transformUrlForDiskCacheKey(url));
                scheduleDiskCacheFlush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    synchronized void setDiskCache(DiskLruCache diskCache) {
        mDiskCache = diskCache;

        if (null != diskCache) {
            mDiskCacheEditLocks = new HashMap<String, ReentrantLock>();
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


    private String parseArrayList(Object o) {
        ArrayList d = (ArrayList)o;
        String className = "";
        if(d.size() > 0) {
            className = className + "<" + d.get(0).getClass().getCanonicalName() + ">";
        }
        return className;
    }

    private void writeValueToDisk(OutputStream os, Object o) throws IOException {

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os));

        String className = o.getClass().getCanonicalName();

        if(o instanceof ArrayList) {
            className = parseArrayList(o);
        } else if(o instanceof HashMap) {
            HashMap d = (HashMap)o;
            if(d.size() > 0) {
                Map.Entry entry = (Map.Entry) d.entrySet().iterator().next();

                if(entry.getValue() instanceof ArrayList) {
                    className = className + "<"+ entry.getKey().getClass().getCanonicalName()+",java.util.ArrayList"+parseArrayList(entry.getValue())+">";
                } else {
                    className = className + "<"+ entry.getKey().getClass().getCanonicalName()+","+entry.getValue().getClass().getCanonicalName()+">";
                }
            }
        }

        out.write(className+"\n");

        if(className.contains("google")) {
            mJsonFactory.createJsonGenerator(out).serialize(o);
        } else {
            String json = mGson.toJson(o);
            out.write(json);
        }

        out.close();
    }

    private long readExpirationFromDisk(InputStream is) throws IOException {
        DataInputStream din = new DataInputStream(is);
        long expiration = din.readLong();
        din.close();
        return expiration;
    }

    private Object readValueFromDisk(InputStream is) throws IOException {
        BufferedReader fss = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String className = fss.readLine();

        if (className == null) {
            return null;
        }

        Type type = null;
        if(className.startsWith("java.util.ArrayList")) {
            String inner = className.substring("java.util.ArrayList<".length(), className.length()-1);
            Class innerClass = null;
            try {
                innerClass = Class.forName(inner);
                type = TypeToken.get(Utils.createListOfType(innerClass).getClass()).getType();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if(className.startsWith("java.util.HashMap")) {
            String[] inner = className.substring("java.util.HashMap<".length(), className.length()-1).split(",");

            try {
                Class keyClass = Class.forName(inner[0]);

                Class valueClass = null;
                if(inner[1].startsWith("java.util.ArrayList")) {
                    String innterT = inner[1].substring("java.util.ArrayList<".length(), inner[1].length() - 1);
                    try {
                        Class innerClass = Class.forName(innterT);
                        Type t3 = TypeToken.get(Utils.createListOfType(innerClass).getClass()).getType();
                        valueClass = (Class) t3;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else {
                    valueClass = Class.forName(inner[1]);
                }

                type = TypeToken.get(Utils.createMapOfType(keyClass, valueClass).getClass()).getType();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        String line = null;
        String content = "";
        while ((line = fss.readLine()) != null) {
            content += line;
        }

        fss.close();

        Class<?> clazz;
        try {

            if(className.contains("google")) {
                clazz = Class.forName(className);
                return mJsonFactory.createJsonParser(content).parseAndClose(clazz, null);
            } else if(type != null) {
                return mGson.fromJson(content, type);
            } else {
                clazz = Class.forName(className);
                return mGson.fromJson(content, clazz);
            }
        } catch(IllegalArgumentException e) {
            Timber.e("Deserializing from disk failed", e);
            return null;
        } catch(ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    private static String transformUrlForDiskCacheKey(String url) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void checkNotOnMainThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException(
                    "This method should not be called from the main/UI thread.");
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

    public interface CachePutListener {
        public void onPutIntoCache();
    }

    public interface CacheListener {
        public void onGet(Object item);
        public void onNotFound(String key);
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
}
