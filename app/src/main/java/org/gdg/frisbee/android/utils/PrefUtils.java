package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.joda.time.DateTime;

public final class PrefUtils {
    public static final String PREF_NAME = "gdg";
    public static final String SETTINGS_ROOT = "root";
    public static final String SETTINGS_HOME_GDG = "gdg_home";
    public static final String SETTINGS_SIGNED_IN = "gdg_signed_in";
    public static final String SETTINGS_ANALYTICS = "analytics";
    private static final String SETTINGS_HOME_GDG_NAME = "gdg_home_name";
    private static final String PREFS_VIDEOS_PLAYED = "gdg_app_videos_played";
    private static final String PREFS_OPEN_DRAWER_ON_START = "open_drawer_on_start";
    private static final String PREFS_FIRST_START = "gdg_first_start";
    private static final String PREFS_VERSION_CODE = "gdg_version_code";
    private static final String PREFS_APP_STARTS = "gdg_app_starts";
    private static final String PREFS_SEASONS_GREETINGS = "seasons_greetings";
    private static final String PREFS_ACHIEVEMENTS_PREFIX = "achievement_unlocked_";
    private static final String PREFS_WIDGET_ADDED = "widget_added";
    private static final String PREFS_FATAL_GOOGLE_PLAY_SERVICE = "fatal_google_play_service";
    private static final String PREFS_EVENT_SERIES_NOTIFICATION_ALARM_SET = "event_series_notification_alarm_set";

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
        App.getInstance().initOrganizer();
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

    public static void setInitialSettings(final Context context, final boolean enableAnalytics) {
        prefs(context).edit()
            .putBoolean(SETTINGS_ANALYTICS, enableAnalytics)
            .putBoolean(PREFS_FIRST_START, false)
            .apply();
    }

    public static boolean isFirstStart(Context context) {
        return prefs(context)
            .getBoolean(PREFS_FIRST_START, PREFS_FIRST_START_DEFAULT);
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

    public static void setVersionCode(final Context context, final int newVersion) {
        prefs(context).edit().putInt(PREFS_VERSION_CODE, newVersion).apply();
    }

    public static void resetInitialSettings(final Context context) {
        prefs(context).edit()
            .clear()
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

    public static boolean hasHigherAchievementSteps(Context context, String achievementName, int steps) {
        SharedPreferences preferences = prefs(context);
        String achievementKey = PREFS_ACHIEVEMENTS_PREFIX + achievementName;
        return preferences.getInt(achievementKey, 0) > steps;
    }

    public static void setAchievementSteps(@NonNull final Context context,
                                           @NonNull final String achievement, @NonNull final Integer steps) {
        prefs(context).edit()
            .putInt(PREFS_ACHIEVEMENTS_PREFIX + achievement, steps)
            .apply();
    }

    public static boolean isWidgetAdded(final Context context) {
        return prefs(context).getBoolean(PREFS_WIDGET_ADDED, false);
    }

    public static void setWidgetAdded(@NonNull final Context context) {
        prefs(context).edit()
            .putBoolean(PREFS_WIDGET_ADDED, true)
            .apply();
    }

    public static boolean shouldShowFatalPlayServiceMessage(Context context) {
        return prefs(context).getBoolean(PREFS_FATAL_GOOGLE_PLAY_SERVICE, true);
    }

    public static void setFatalPlayServiceMessageShown(Context context) {
        prefs(context).edit().putBoolean(PREFS_FATAL_GOOGLE_PLAY_SERVICE, false).apply();
    }

    public static boolean isTaggedEventSeriesAlarmSet(Context context, TaggedEventSeries taggedEventSeries) {
        long lastTimeSent = prefs(context).getLong(keyForEventSeriesAlarmSet(taggedEventSeries), 0);
        return lastTimeSent >= taggedEventSeries.getStartDate().getMillis();
    }

    public static void setTaggedEventSeriesAlarmTime(Context context, TaggedEventSeries taggedEventSeries) {
        long currentSeriesStartTime = taggedEventSeries.getStartDate().getMillis();
        prefs(context).edit()
            .putLong(keyForEventSeriesAlarmSet(taggedEventSeries), currentSeriesStartTime)
            .apply();
    }

    @NonNull
    private static String keyForEventSeriesAlarmSet(TaggedEventSeries taggedEventSeries) {
        return PREFS_EVENT_SERIES_NOTIFICATION_ALARM_SET + taggedEventSeries.getTag();
    }
}
