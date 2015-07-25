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

package org.gdg.frisbee.android.app;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.gson.FieldNamingPolicy;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.achievements.AchievementActionHandler;
import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.deserializer.ZuluDateTimeDeserializer;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.gdg.frisbee.android.utils.CrashlyticsTree;
import org.gdg.frisbee.android.utils.GingerbreadLastLocationFinder;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import timber.log.Timber;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 20.04.13
 * Time: 12:09
 */
public class App extends Application implements LocationListener,
        GoogleApiClient.ConnectionCallbacks {

    private static App mInstance = null;

    public static App getInstance() {
        return mInstance;
    }

    private GroupDirectory groupDirectoryInstance;
    private GdgXHub hubInstance;
    private ModelCache mModelCache;
    private Picasso mPicasso;
    private Tracker mTracker;
    private GingerbreadLastLocationFinder mLocationFinder;
    private Location mLastLocation;
    private OrganizerChecker mOrganizerChecker;
    private ArrayList<TaggedEventSeries> mTaggedEventSeriesList;
    private RefWatcher refWatcher;

    private GoogleApiClient mGoogleApiClient;
    private AchievementActionHandler mAchievementActionHandler;
    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());

            StrictMode.ThreadPolicy.Builder b = new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyFlashScreen();

            StrictMode.setThreadPolicy(b.build());
        } else {
            Fabric.with(this, new Crashlytics());
            Timber.plant(new CrashlyticsTree());
        }

        mInstance = this;

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            int versionCode = PrefUtils.getVersionCode(this);
            if (versionCode < pInfo.versionCode) {
                migrate(versionCode, pInfo.versionCode);
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Initialize ModelCache and Volley
        getModelCache();

        PrefUtils.increaseAppStartCount(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Plus.API)
                .addApi(Games.API)
                .addApi(AppStateManager.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addScope(Games.SCOPE_GAMES)
                .build();

        mAchievementActionHandler =
                new AchievementActionHandler(mHandler, getGoogleApiClient(), this);

        // Initialize Picasso
        mPicasso = new Picasso.Builder(this)
                .downloader(new OkHttpDownloader(this))
                .memoryCache(new LruCache(this))
                .build();
        mPicasso.setIndicatorsEnabled(BuildConfig.DEBUG);

        JodaTimeAndroid.init(this);

        refWatcher = LeakCanary.install(this);

        mOrganizerChecker = new OrganizerChecker(PrefUtils.prefs(this));

        GoogleAnalytics.getInstance(this).setAppOptOut(PrefUtils.isAnalyticsEnabled(this));

        // Init LastLocationFinder
        mLocationFinder = new GingerbreadLastLocationFinder(this);
        mLocationFinder.setChangedLocationListener(this);
        updateLastLocation();

        initTaggedEventSeries();
    }


    /**
     * Init TaggedEventSeries.
     */
    private void initTaggedEventSeries() {

        mTaggedEventSeriesList = new ArrayList<>();
        //Add Women Techmakers
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
                R.style.Theme_GDG_Special_Wtm,
                "wtm",
                Const.START_TIME_WTM,
                Const.END_TIME_WTM));
        //Add Android Fundamentals Study Jams
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
                R.style.Theme_GDG_Special_StudyJams,
                "studyjam",
                Const.START_TIME_STUDY_JAMS,
                Const.END_TIME_STUDY_JAMS));
        //Add IO Extended
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
                R.style.Theme_GDG_Special_IOExtended,
                "i-oextended",
                Const.START_TIME_IOEXTENDED,
                Const.END_TIME_IOEXTENDED));
    }

    private void addTaggedEventSeriesIfDateFits(@NonNull TaggedEventSeries taggedEventSeries) {
        DateTime now = DateTime.now();
        if (BuildConfig.DEBUG || (now.isAfter(taggedEventSeries.getStartDateInMillis())
                && now.isBefore(taggedEventSeries.getEndDateInMillis()))) {
            mTaggedEventSeriesList.add(taggedEventSeries);
        }
    }

    private void migrate(int oldVersion, int newVersion) {

        if (oldVersion < 11100 || BuildConfig.ALPHA) {
            PrefUtils.resetInitalSettings(this);
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                // SD-card available
                String rootDirExt = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getPackageName() + "/cache";
                deleteDirectory(new File(rootDirExt));
            }

            File internalCacheDir = getCacheDir();
            deleteDirectory(new File(internalCacheDir.getAbsolutePath()));

            if (BuildConfig.ALPHA) {
                Toast.makeText(getApplicationContext(), "Alpha version always resets Preferences on update.", Toast.LENGTH_LONG).show();
            }
        }
        PrefUtils.setVersionCode(this, newVersion);
    }

    public void updateLastLocation() {
        if (Utils.isEmulator()) {
            return;
        }

        Location loc = mLocationFinder.getLastBestLocation(5000, 60 * 60 * 1000);

        if (loc != null) {
            mLastLocation = loc;
        }
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public Tracker getTracker() {
        if (mTracker == null) {
            // Initialize GA
            GoogleAnalytics mGaInstance = GoogleAnalytics.getInstance(getApplicationContext());
            mTracker = mGaInstance.newTracker(getString(R.string.ga_trackingId));

            mTracker.setAppName(getString(R.string.app_name));
            mTracker.setAnonymizeIp(true);
        }

        return mTracker;
    }

    public ModelCache getModelCache() {
        if (mModelCache == null) {

            final File rootDir;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                // SD-card available
                rootDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/Android/data/" + getPackageName() + "/model_cache/");
            } else {
                File internalCacheDir = getCacheDir();
                rootDir = new File(internalCacheDir.getAbsolutePath() + "/model_cache/");
            }

            if (rootDir.mkdirs() || rootDir.isDirectory()) {
                mModelCache = new ModelCache.Builder(getApplicationContext())
                        .setMemoryCacheEnabled(true)
                        .setDiskCacheEnabled(true)
                        .setDiskCacheLocation(rootDir)
                        .build();
            }
        }
        return mModelCache;
    }

    private boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File child : dir.listFiles()) {
                deleteDirectory(child);
            }
        }

        return dir.delete();
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

    public boolean isOrganizer() {
        return mOrganizerChecker.isOrganizer();
    }

    public void checkOrganizer(GoogleApiClient apiClient, OrganizerChecker.Callbacks responseHandler) {
        mOrganizerChecker.checkOrganizer(apiClient, responseHandler);
    }

    /**
     * Return the current list of GDG event series occurring in the world.
     * This may be empty but cannot be null.
     *
     * @return ArrayList of current event series.
     */
    @NonNull
    public ArrayList<TaggedEventSeries> currentTaggedEventSeries() {
        return mTaggedEventSeriesList;
    }

    public GdgXHub getGdgXHub() {
        if (hubInstance == null) {
            hubInstance = new RestAdapter.Builder()
                    .setEndpoint(GdgXHub.BASE_URL)
                    .setConverter(new GsonConverter(Utils.getGson(FieldNamingPolicy.IDENTITY, new ZuluDateTimeDeserializer())))
                    .build().create(GdgXHub.class);
        }
        return hubInstance;
    }

    public GroupDirectory getGroupDirectory() {
        if (groupDirectoryInstance == null) {
            groupDirectoryInstance = new RestAdapter.Builder()
                    .setEndpoint(GroupDirectory.BASE_URL)
                    .setConverter(new GsonConverter(Utils.getGson()))
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("User-Agent", "GDG-Frisbee/0.1 (Android)");
                            request.addHeader("Referer", "https://developers.google.com/groups/directory/");
                            request.addHeader("X-Requested-With", "XMLHttpRequest");
                            request.addHeader("Cache-Control", "no-cache");
                            request.addHeader("DNT", "1");
                        }
                    })
                    .build().create(GroupDirectory.class);
        }
        return groupDirectoryInstance;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }

    @NonNull
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public AchievementActionHandler getAchievementActionHandler() {
        return mAchievementActionHandler;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mAchievementActionHandler.onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }
}
