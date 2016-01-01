package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.app.App;
import org.joda.time.DateTime;

public final class PrefUtils {
    public static final String PREF_NAME = "gdg";
    public static final String SETTINGS_HOME_GDG = "gdg_home";
    private static final String SETTINGS_HOME_GDG_NAME = "gdg_home_name";
    public static final String SETTINGS_GCM = "gcm";
    public static final String SETTINGS_SIGNED_IN = "gdg_signed_in";
    public static final String SETTINGS_ANALYTICS = "analytics";
    private static final String PREFS_VIDEOS_PLAYED = "gdg_app_videos_played";
    private static final String PREFS_OPEN_DRAWER_ON_START = "open_drawer_on_start";
    private static final String PREFS_FIRST_START = "gdg_first_start";
    private static final String PREFS_GCM_REG_ID = "gdg_registration_id";
    private static final String PREFS_VERSION_CODE = "gdg_version_code";
    private static final String PREFS_APP_STARTS = "gdg_app_starts";
    private static final String PREFS_GCM_NOTIFICATION_KEY = "gcm_notification_key";
    private static final String PREFS_SEASONS_GREETINGS = "seasons_greetings";
    private static final String PREFS_ACHIEVEMENTS_PREFIX = "achievement_unlocked_";
    private static final String PREFS_LAST_PLAY_SERVICE_ERROR_STATUS = "last_play_service_error_status";

    private static final boolean PREFS_FIRST_START_DEFAULT = true;

    private PrefUtils() {
        // Prevent instances of this class being created.
    }

    public static SharedPreferences prefs(final Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean shouldOpenDrawerOnStart(Context context) {
        return prefs(context).getBoolean(PREFS_OPEN_DRAWER_ON_START, true);
    }

    public static void setShouldNotOpenDrawerOnStart(Context context) {
        prefs(context).edit().putBoolean(PREFS_OPEN_DRAWER_ON_START, false).apply();
    }

    public static boolean isSignedIn(Context context) {
        return prefs(context).getBoolean(SETTINGS_SIGNED_IN, false);
    }

    public static void setSignedIn(Context context) {
        prefs(context).edit().putBoolean(SETTINGS_SIGNED_IN, true).apply();
    }

    public static void setLoggedOut(Context context) {
        prefs(context).edit()
                .putBoolean(SETTINGS_SIGNED_IN, false)
                .putBoolean(Const.PREF_ORGANIZER_STATE, false)
                .putLong(Const.PREF_ORGANIZER_CHECK_TIME, 0)
                .apply();
        App.getInstance().resetOrganizer();
    }

    @Nullable
    public static String getHomeChapterId(final Context context) {
        return prefs(context)
                .getString(SETTINGS_HOME_GDG, null);
    }

    @NonNull
    public static String getHomeChapterIdNotNull(final Context context) {
        return prefs(context)
                .getString(SETTINGS_HOME_GDG, "");
    }

    public static void setHomeChapter(final Context context, final Chapter chapter) {
        prefs(context).edit()
                .putString(SETTINGS_HOME_GDG, chapter.getGplusId())
                .putString(SETTINGS_HOME_GDG_NAME, chapter.getName())
                .apply();
    }

    public static void setInitialSettings(final Context context, final boolean enableGcm, final boolean enableAnalytics,
                                          final String regid, final String notificationKey) {
        prefs(context).edit()
                .putBoolean(SETTINGS_GCM, enableGcm)
                .putBoolean(SETTINGS_ANALYTICS, enableAnalytics)
                .putBoolean(PREFS_FIRST_START, false)
                .putString(PREFS_GCM_REG_ID, regid)
                .putString(PREFS_GCM_NOTIFICATION_KEY, notificationKey)
                .apply();
    }

    public static boolean isFirstStart(Context context) {
        return prefs(context)
                .getBoolean(PREFS_FIRST_START, PREFS_FIRST_START_DEFAULT);
    }

    public static boolean isGcmEnabled(Context context) {
        return prefs(context)
                .getBoolean(PREFS_FIRST_START, false);
    }

    @NonNull
    public static String getRegistrationId(Context context) {
        String registrationId = prefs(context).getString(PREFS_GCM_REG_ID, null);
        if (TextUtils.isEmpty(registrationId)) {
            return "";
        }
        return registrationId;
    }

    public static void increaseAppStartCount(final Context context) {
        increaseIntPreference(context, PREFS_APP_STARTS);
    }

    public static int increaseVideoViewed(final Context mContext) {
        return increaseIntPreference(mContext, PREFS_VIDEOS_PLAYED);
    }

    private static int increaseIntPreference(final Context context, String key) {
        int newValue = prefs(context).getInt(key, 0) + 1;
        prefs(context).edit().putInt(key, newValue).apply();
        return newValue;
    }

    public static boolean isAnalyticsEnabled(final Context context) {
        return prefs(context).getBoolean(SETTINGS_ANALYTICS, false);
    }

    public static int getVersionCode(final Context context) {
        return prefs(context).getInt(PREFS_VERSION_CODE, 0);
    }

    public static int getAppStarts(final Context context) {
        return prefs(context).getInt(PREFS_APP_STARTS, 0);
    }

    public static boolean shouldShowSeasonsGreetings(final Context context) {
        DateTime now = DateTime.now();
        if (prefs(context).getInt(PREFS_SEASONS_GREETINGS, now.getYear() - 1) < now.getYear()
                && (now.getDayOfYear() >= 354 && now.getDayOfYear() <= 366)) {
            prefs(context).edit().putInt(PREFS_SEASONS_GREETINGS, now.getYear()).apply();
            return true;
        }
        return false;
    }

    public static void setGcmSettings(final Context context, final boolean enabledGcm, final String regId, final String notificationKey) {
        prefs(context).edit()
                .putBoolean(SETTINGS_GCM, enabledGcm)
                .putString(PREFS_GCM_REG_ID, regId)
                .putString(PREFS_GCM_NOTIFICATION_KEY, notificationKey)
                .apply();
    }

    public static void setVersionCode(final Context context, final int newVersion) {
        prefs(context).edit().putInt(PREFS_VERSION_CODE, newVersion).apply();
    }

    public static void resetInitialSettings(final Context context) {
        prefs(context).edit()
                .remove(PREFS_GCM_REG_ID)
                .clear()
                .putBoolean(PREFS_FIRST_START, true)
                .apply();
    }

    public static boolean isAchievementUnlocked(final Context context, final String achievement) {
        return prefs(context).getBoolean(PREFS_ACHIEVEMENTS_PREFIX + achievement, false);
    }

    public static void setAchievementUnlocked(@NonNull final Context context,
                                              @NonNull final String achievement) {
        prefs(context).edit()
                .putBoolean(PREFS_ACHIEVEMENTS_PREFIX + achievement, true)
                .apply();
    }

    public static int getLastPlayServiceErrorStatus(final Context context) {
        return prefs(context).getInt(PREFS_LAST_PLAY_SERVICE_ERROR_STATUS, 0);
    }

    public static void setLastPlayServiceErrorStatus(Context context, int playServiceStatus) {
        prefs(context).edit()
                .putInt(PREFS_LAST_PLAY_SERVICE_ERROR_STATUS, playServiceStatus)
                .apply();
    }
}
