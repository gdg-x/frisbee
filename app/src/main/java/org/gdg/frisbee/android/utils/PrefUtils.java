package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefUtils {
    private static final String PREF_NAME = "gdg";
    private static final String PREF_OPEN_DRAWER_ON_START = "open_drawer_on_start";
    private static final boolean PREF_OPEN_DRAWER_ON_START_DEFAULT = true;
    private static final String PREF_SIGNED_IN = "gdg_signed_in";

    private PrefUtils() {
        // Prevent instances of this class being created.
    }

    public static boolean isFirstStartDone(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_OPEN_DRAWER_ON_START, PREF_OPEN_DRAWER_ON_START_DEFAULT);
    }

    public static void setFirstStartDone(Context context) {
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
}
