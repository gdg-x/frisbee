package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.gdg.frisbee.android.Const;

public final class PrefUtils {
    private static final String PREF_NAME = "gdg";
    private static final String PREF_OPEN_DRAWER_ON_START = "open_drawer_on_start";
    private static final String PREF_SIGNED_IN = "gdg_signed_in";

    private PrefUtils() {
        // Prevent instances of this class being created.
    }

    public static boolean shouldOpenDrawerOnStart(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_OPEN_DRAWER_ON_START, true);
    }

    public static void setShouldNotOpenDrawerOnStart(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_OPEN_DRAWER_ON_START, false).apply();
    }

    public static boolean isSignedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_SIGNED_IN, false);
    }

    public static void setSignedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_SIGNED_IN, true).apply();
    }

    public static void setLoggedOut(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_SIGNED_IN, false).apply();
    }

    public static String getHomeChapterId(final Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(Const.SETTINGS_HOME_GDG, null);
    }

    public static void setHomeChapterId(final Context context, final String chapterGplusId) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString(Const.SETTINGS_HOME_GDG, chapterGplusId)
                .apply();
    }

    public static void setInitialSettings(final Context context, final boolean enableGcm, final boolean enableAnalytics,
                                          final String regid, final String notificationKey) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                .putBoolean(Const.SETTINGS_GCM, enableGcm)
                .putBoolean(Const.SETTINGS_ANALYTICS, enableAnalytics)
                .putBoolean(Const.SETTINGS_FIRST_START, false)
                .putString(Const.SETTINGS_GCM_REG_ID, regid)
                .putString(Const.SETTINGS_GCM_NOTIFICATION_KEY, notificationKey)
                .apply();
    }

    public static boolean isFirstStart(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(Const.SETTINGS_FIRST_START, true);
    }
}
