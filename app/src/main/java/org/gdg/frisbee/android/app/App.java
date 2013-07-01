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

package org.gdg.frisbee.android.app;

import android.app.Application;
import android.os.Environment;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpLoader;
import com.squareup.picasso.Picasso;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.cache.ModelCache;
import uk.co.senab.bitmapcache.BitmapLruCache;

import java.io.File;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 20.04.13
 * Time: 12:09
 */
public class App extends Application {

    private static App mInstance = null;

    public static App getInstance() {
        return mInstance;
    }

    private BitmapLruCache mBitmapCache;
    private ModelCache mModelCache;
    private Picasso mPicasso;

    private GoogleAnalytics mGaInstance;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        // Workaround for OkHttp Bug #184. Do it only once
        if(mInstance == null)
            URL.setURLStreamHandlerFactory(new OkHttpClient());

        mInstance = this;
        getModelCache();
        getBitmapCache();
        GdgVolley.init(this);

        mPicasso = new Picasso.Builder(this)
                .loader(new OkHttpLoader(this))
                .memoryCache(new LruCache(this))
                .build();
        mPicasso.setDebugging(false);

        mGaInstance = GoogleAnalytics.getInstance(getApplicationContext());
        mGaInstance.setDebug(true);
        mTracker = mGaInstance.getTracker(getString(R.string.ga_trackingId));
        mTracker.setAppName(getString(R.string.app_name));
        mTracker.setAnonymizeIp(true);
        mGaInstance.setDefaultTracker(mTracker);
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public Tracker getTracker() {
        return mTracker;
    }

    public ModelCache getModelCache() {
        if(mModelCache == null) {

            File rootDir = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                // SD-card available
                rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getPackageName() + "/model_cache/");
            } else {
                File internalCacheDir = getCacheDir();
                rootDir = new File(internalCacheDir.getAbsolutePath() + "/model_cache/");
            }

            rootDir.mkdirs();

            mModelCache = new ModelCache.Builder(getApplicationContext())
                    .setMemoryCacheEnabled(true)
                    .setDiskCacheEnabled(true)
                    .setDiskCacheLocation(rootDir)
                    .build();
        }
        return mModelCache;
    }

    public BitmapLruCache getBitmapCache() {
        if(mBitmapCache == null) {

            String rootDir = null;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                // SD-card available
                rootDir = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getPackageName() + "/cache";
            } else {
                File internalCacheDir = getCacheDir();
                rootDir = internalCacheDir.getAbsolutePath();
            }

            mBitmapCache = new BitmapLruCache.Builder(getApplicationContext())
                    .setMemoryCacheEnabled(true)
                    .setMemoryCacheMaxSizeUsingHeapSize()
                    .build();
        }
        return mBitmapCache;
    }
}
