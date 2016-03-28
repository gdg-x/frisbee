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

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GdeDirectory;
import org.gdg.frisbee.android.api.GdeDirectoryFactory;
import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.GdgXHubFactory;
import org.gdg.frisbee.android.api.GitHub;
import org.gdg.frisbee.android.api.GithubFactory;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.GroupDirectoryFactory;
import org.gdg.frisbee.android.api.OkClientFactory;
import org.gdg.frisbee.android.api.PlusApi;
import org.gdg.frisbee.android.api.PlusApiFactory;
import org.gdg.frisbee.android.api.PlusImageUrlConverter;
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
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class App extends BaseApp implements LocationListener {

    private static App mInstance = null;
    private OkHttpClient mOkHttpClient;
    private GroupDirectory groupDirectoryInstance;
    private GdgXHub hubInstance;
    private GdeDirectory gdeDirectoryInstance;
    private GitHub gitHubInstance;
    private ModelCache mModelCache;
    private Picasso mPicasso;
    private Tracker mTracker;
    private GingerbreadLastLocationFinder mLocationFinder;
    private Location mLastLocation;
    private OrganizerChecker mOrganizerChecker;
    private ArrayList<TaggedEventSeries> mTaggedEventSeriesList;
    private RefWatcher refWatcher;
    private PlusApi plusApiInstance;

    public static App getInstance() {
        return mInstance;
    }

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
            Crashlytics.setString("commitSha", BuildConfig.COMMIT_SHA);
            Crashlytics.setString("commitTime", BuildConfig.COMMIT_TIME);
            Timber.plant(new CrashlyticsTree());
        }

        mInstance = this;

        int storedVersionCode = PrefUtils.getVersionCode(this);
        if (storedVersionCode != 0 && storedVersionCode < BuildConfig.VERSION_CODE) {
            onAppUpdate(storedVersionCode, BuildConfig.VERSION_CODE);
            PrefUtils.setVersionCode(this, BuildConfig.VERSION_CODE);
        }
        mOkHttpClient = OkClientFactory.provideOkHttpClient(this);

        // Initialize ModelCache
        getModelCache();

        PrefUtils.increaseAppStartCount(this);

        // Initialize Picasso
        // When we clone mOkHttpClient, it will use all the same cache and everything.
        // Only the interceptors will be different.
        // We shouldn't have the below interceptor in other instances.
        OkHttpClient.Builder picassoClient = mOkHttpClient.newBuilder();
        picassoClient.addInterceptor(new PlusImageUrlConverter());

        mPicasso = new Picasso.Builder(this)
            .downloader(new OkHttp3Downloader(picassoClient.build()))
            .memoryCache(new LruCache(this))
            .build();

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
        //Add DevFest
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
            R.style.Theme_GDG_Special_DevFest,
            "devfest",
            Const.DRAWER_DEVFEST,
            Const.START_TIME_DEVFEST,
            Const.END_TIME_DEVFEST));
        //Add Women Techmakers
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
            R.style.Theme_GDG_Special_Wtm,
            "wtm",
            Const.DRAWER_WTM,
            Const.START_TIME_WTM,
            Const.END_TIME_WTM));
        //Add Android Fundamentals Study Jams
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
            R.style.Theme_GDG_Special_StudyJams,
            "studyjam",
            Const.DRAWER_STUDY_JAM,
            Const.START_TIME_STUDY_JAMS,
            Const.END_TIME_STUDY_JAMS));
        //Add IO Extended
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
            R.style.Theme_GDG_Special_IOExtended,
            "i-oextended",
            Const.DRAWER_IO_EXTENDED,
            Const.START_TIME_IOEXTENDED,
            Const.END_TIME_IOEXTENDED));
        //Add GCP NEXT
        addTaggedEventSeriesIfDateFits(new TaggedEventSeries(this,
            R.style.Theme_GDG_Special_GCPNEXT,
            "gcpnext",
            Const.DRAWER_GCP_NEXT,
            Const.START_TIME_GCP_NEXT,
            Const.END_TIME_GCP_NEXT));

    }

    private void addTaggedEventSeriesIfDateFits(@NonNull TaggedEventSeries taggedEventSeries) {
        DateTime now = DateTime.now();
        if (BuildConfig.DEBUG || (now.isAfter(taggedEventSeries.getStartDateInMillis())
            && now.isBefore(taggedEventSeries.getEndDateInMillis()))) {
            mTaggedEventSeriesList.add(taggedEventSeries);
        }
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
            GoogleAnalytics gaInstance = GoogleAnalytics.getInstance(getApplicationContext());
            mTracker = gaInstance.newTracker(getString(R.string.ga_trackingId));

            mTracker.setAppName(getString(R.string.app_name));
            mTracker.setAnonymizeIp(true);
        }

        return mTracker;
    }

    @NonNull
    public ModelCache getModelCache() {
        if (mModelCache == null) {

            File cacheDir = getExternalCacheDir();
            if (cacheDir == null) {
                cacheDir = getCacheDir();
            }
            final File rootDir = new File(cacheDir, "/model_cache/");

            ModelCache.Builder builder = new ModelCache.Builder(this)
                .setMemoryCacheEnabled(true);
            if (rootDir.isDirectory() || rootDir.mkdirs()) {
                builder.setDiskCacheEnabled(true)
                    .setDiskCacheLocation(rootDir);
            }
            mModelCache = builder.build();
        }
        return mModelCache;
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

    public void initOrganizer() {
        mOrganizerChecker.initOrganizer();
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
            hubInstance = GdgXHubFactory.provideHubApi();
        }
        return hubInstance;
    }

    public GroupDirectory getGroupDirectory() {
        if (groupDirectoryInstance == null) {
            groupDirectoryInstance = GroupDirectoryFactory.provideGroupDirectoryApi();
        }
        return groupDirectoryInstance;
    }

    public GdeDirectory getGdeDirectory() {
        if (gdeDirectoryInstance == null) {
            gdeDirectoryInstance = GdeDirectoryFactory.provideGdeApi();
        }
        return gdeDirectoryInstance;
    }

    public GitHub getGithub() {
        if (gitHubInstance == null) {
            gitHubInstance = GithubFactory.provideGitHubApi();
        }
        return gitHubInstance;
    }

    public PlusApi getPlusApi() {
        if (plusApiInstance == null) {
            plusApiInstance = PlusApiFactory.providePlusApi();
        }
        return plusApiInstance;
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }
}
