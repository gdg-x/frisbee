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

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.squareup.picasso.Picasso;

import net.danlew.android.joda.JodaTimeAndroid;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.FirebaseDynamicLinksHub;
import org.gdg.frisbee.android.api.FirebaseDynamicLinksHubFactory;
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
import org.gdg.frisbee.android.eventseries.NotificationHandler;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.gdg.frisbee.android.eventseries.TaggedEventSeriesFactory;
import org.gdg.frisbee.android.utils.CrashlyticsTree;
import org.gdg.frisbee.android.utils.FileUtils;
import org.gdg.frisbee.android.utils.GingerbreadLastLocationFinder;
import org.gdg.frisbee.android.utils.PrefUtils;

import java.io.File;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class App extends BaseApp implements LocationListener {

    private OkHttpClient okHttpClient;
    private GroupDirectory groupDirectory;
    private GdgXHub gdgXHub;
    private GdeDirectory gdeDirectory;
    private GitHub gitHub;
    private PlusApi plusApi;
    private ModelCache mModelCache;
    private Picasso mPicasso;
    private Tracker mTracker;
    private GingerbreadLastLocationFinder mLocationFinder;
    private Location mLastLocation;
    private OrganizerChecker mOrganizerChecker;
    private List<TaggedEventSeries> mTaggedEventSeriesList;
    private RefWatcher refWatcher;
    private FirebaseDynamicLinksHub firebaseDynamicLinksHub;

    public static App from(Context context) {
        return (App) context.getApplicationContext();
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

        int storedVersionCode = PrefUtils.getVersionCode(this);
        if (storedVersionCode != 0 && storedVersionCode < BuildConfig.VERSION_CODE) {
            onAppUpdate(storedVersionCode, BuildConfig.VERSION_CODE);
            PrefUtils.setVersionCode(this, BuildConfig.VERSION_CODE);
        }
        okHttpClient = OkClientFactory.provideOkHttpClient(this);

        // Initialize ModelCache
        getModelCache();

        PrefUtils.increaseAppStartCount(this);

        // Initialize Picasso
        OkHttpClient picassoClient = okHttpClient.newBuilder()
            .cache(OkHttp3Downloader.createDefaultCache(this))
            .addInterceptor(new PlusImageUrlConverter(getPlusApi()))
            .build();
        mPicasso = new Picasso.Builder(this)
            .downloader(new OkHttp3Downloader(picassoClient))
            .build();

        JodaTimeAndroid.init(this);

        refWatcher = LeakCanary.install(this);

        mOrganizerChecker = new OrganizerChecker(PrefUtils.prefs(this), getGdgXHub());

        GoogleAnalytics.getInstance(this).setAppOptOut(PrefUtils.isAnalyticsEnabled(this));

        // Init LastLocationFinder
        mLocationFinder = new GingerbreadLastLocationFinder(this);
        mLocationFinder.setChangedLocationListener(this);
        updateLastLocation();

        initTaggedEventSeries();
    }

    @Override
    protected void onAppUpdate(int oldVersion, int newVersion) {
        super.onAppUpdate(oldVersion, newVersion);

        File diskCacheLocation = getDiskCacheLocation();
        FileUtils.deleteDirectory(diskCacheLocation);
    }

    /**
     * Init TaggedEventSeries.
     */
    private void initTaggedEventSeries() {
        mTaggedEventSeriesList = TaggedEventSeriesFactory.createAvailableEventSeries(this);

        updateEventSeriesAlarms();

    }

    private void updateEventSeriesAlarms() {
        for (TaggedEventSeries eventSeries : currentTaggedEventSeries()) {
            NotificationHandler notificationHandler = new NotificationHandler(this, eventSeries);
            if (notificationHandler.shouldSetAlarm()) {
                notificationHandler.setAlarmForNotification();
            }
        }
    }

    public void updateLastLocation() {
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

    public ModelCache getModelCache() {
        if (mModelCache == null) {

            final File rootDir = getDiskCacheLocation();

            ModelCache.Builder builder = new ModelCache.Builder(this)
                .setMemoryCacheEnabled(true);
            if (rootDir.mkdirs() || rootDir.isDirectory()) {
                builder.setDiskCacheEnabled(true)
                    .setDiskCacheLocation(rootDir);
            }
            mModelCache = builder.build();
        }
        return mModelCache;
    }

    private File getDiskCacheLocation() {
        File cacheDir = getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = getCacheDir();
        }
        return new File(cacheDir, "/model_cache/");
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

    public void checkOrganizer(OrganizerChecker.Callbacks responseHandler) {
        mOrganizerChecker.checkOrganizer(this, responseHandler);
    }

    public void resetOrganizer() {
        mOrganizerChecker.resetOrganizer();
    }

    /**
     * Return the current list of GDG event series occurring in the world.
     * This may be empty but cannot be null.
     *
     * @return List of current event series.
     */
    public List<TaggedEventSeries> currentTaggedEventSeries() {
        return mTaggedEventSeriesList;
    }

    public GdgXHub getGdgXHub() {
        if (gdgXHub == null) {
            gdgXHub = GdgXHubFactory.provideHubApi(okHttpClient);
        }
        return gdgXHub;
    }

    public GroupDirectory getGroupDirectory() {
        if (groupDirectory == null) {
            groupDirectory = GroupDirectoryFactory.provideGroupDirectoryApi(okHttpClient);
        }
        return groupDirectory;
    }

    public GdeDirectory getGdeDirectory() {
        if (gdeDirectory == null) {
            gdeDirectory = GdeDirectoryFactory.provideGdeApi(okHttpClient);
        }
        return gdeDirectory;
    }

    public GitHub getGithub() {
        if (gitHub == null) {
            gitHub = GithubFactory.provideGitHubApi(okHttpClient);
        }
        return gitHub;
    }

    public PlusApi getPlusApi() {
        if (plusApi == null) {
            plusApi = PlusApiFactory.providePlusApi(okHttpClient);
        }
        return plusApi;
    }

    public FirebaseDynamicLinksHub getFirebaseDynamicLinksHub() {
        if (firebaseDynamicLinksHub == null) {
            firebaseDynamicLinksHub = FirebaseDynamicLinksHubFactory.provideFirebaseDynamicLinksApi(okHttpClient);
        }
        return firebaseDynamicLinksHub;
    }

    public RefWatcher getRefWatcher() {
        return refWatcher;
    }
}
