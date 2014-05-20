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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.widget.Toast;

import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URL;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.GingerbreadLastLocationFinder;
import org.gdg.frisbee.android.utils.Utils;

import uk.co.senab.bitmapcache.BitmapLruCache;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 20.04.13
 * Time: 12:09
 */

@ReportsCrashes(httpMethod = HttpSender.Method.POST, reportType = HttpSender.Type.JSON, formUri = "https://gdg-x.hp.af.cm/api/v1/crashreport", formKey = "", disableSSLCertValidation = true)
public class App extends Application implements LocationListener {

    private static App mInstance = null;
    private static boolean mFix = false;
    private static VideoCastManager mVideoCastManager;

    public static App getInstance() {
        return mInstance;
    }

    private BitmapLruCache mBitmapCache;
    private ModelCache mModelCache;
    private Picasso mPicasso;
    private SharedPreferences mPreferences;
    private GoogleAnalytics mGaInstance;
    private Tracker mTracker;
    private GingerbreadLastLocationFinder mLocationFinder;
    private Location mLastLocation;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Const.DEVELOPER_MODE) {
            StrictMode.ThreadPolicy.Builder b = new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    b.penaltyFlashScreen();
            }
            StrictMode.setThreadPolicy(b.build());

        }

        // Initialize ACRA Bugreporting (reports get send to GDG[x] Hub)
        ACRA.init(this);

        // Workaround for OkHttp Bug #184. Do it only once
        if(mFix == false) {
            URL.setURLStreamHandlerFactory(new OkHttpClient());
            mFix = true;
        }

        mInstance = this;

        mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            if(mPreferences.getInt(Const.SETTINGS_VERSION_CODE, 0) < pInfo.versionCode)
                migrate(mPreferences.getInt(Const.SETTINGS_VERSION_CODE, pInfo.versionCode), pInfo.versionCode);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Initialize ModelCache and Volley
        getModelCache();
        getBitmapCache();
        GdgVolley.init(this);

        mPreferences.edit().putInt(Const.SETTINGS_APP_STARTS, mPreferences.getInt(Const.SETTINGS_APP_STARTS,0)+1).apply();

        new Thread(){
            @Override
            public void run() {
                // Initialize Picasso
                mPicasso = new Picasso.Builder(App.this)
                        //.downloader(new CompatOkHttpLoader(this))
                        .memoryCache(new LruCache(App.this))
                        .build();
                mPicasso.setDebugging(Const.DEVELOPER_MODE);
            }
        }.start();

        // Initialize GA
        mGaInstance = GoogleAnalytics.getInstance(getApplicationContext());
        mTracker = mGaInstance.getTracker(getString(R.string.ga_trackingId));
        GAServiceManager.getInstance().setDispatchPeriod(0);
        mTracker.setAppName(getString(R.string.app_name));
        mTracker.setAnonymizeIp(true);
        mGaInstance.setDefaultTracker(mTracker);

        GoogleAnalytics.getInstance(this).setAppOptOut(mPreferences.getBoolean("analytics",false));

        // Init LastLocationFinder
        mLocationFinder = new GingerbreadLastLocationFinder(this);
        mLocationFinder.setChangedLocationListener(this);
        updateLastLocation();
    }

    public void migrate(int oldVersion, int newVersion) {

        mPreferences.edit().remove(Const.SETTINGS_GCM_REG_ID).apply();

        mPreferences.edit().clear().apply();
        mPreferences.edit().putBoolean(Const.SETTINGS_FIRST_START, true);
        String rootDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // SD-card available
            rootDir = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/data/" + getPackageName() + "/cache";
        } else {
            File internalCacheDir = getCacheDir();
            rootDir = internalCacheDir.getAbsolutePath();
        }
        deleteDirectory(new File(rootDir));

        Toast.makeText(getApplicationContext(), "Alpha version always resets Preferences on update.", Toast.LENGTH_LONG).show();

        mPreferences.edit().putInt(Const.SETTINGS_VERSION_CODE, newVersion).apply();
    }

    public void updateLastLocation() {
        if(Utils.isEmulator())
            return;

        Location loc = mLocationFinder.getLastBestLocation(5000,60*60*1000);

        if(loc != null)
            mLastLocation = loc;
    }

    public Location getLastLocation() {
        return mLastLocation;
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

    private void deleteDirectory(File dir) {
        if (dir.isDirectory())
            for (File child : dir.listFiles())
                deleteDirectory(child);

        dir.delete();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    public static VideoCastManager getVideoCastManager(Context context){
        if (null == mVideoCastManager) {
            mVideoCastManager = VideoCastManager.initialize(context, CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID, null, null);
            mVideoCastManager.enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
                    VideoCastManager.FEATURE_LOCKSCREEN |
                    VideoCastManager.FEATURE_DEBUGGING);
        }
        mVideoCastManager.setContext(context);
        return mVideoCastManager;
    }
}
