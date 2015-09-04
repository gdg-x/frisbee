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

package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.deserializer.DateTimeDeserializer;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.utils
 * <p/>
 * User: maui
 * Date: 21.04.13
 * Time: 22:34
 */
public class Utils {
    
    private Utils() {

    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Converts dp value to px value.
     *
     * @param res Resources objects to get displayMetrics.
     * @param dp original dp value.
     * @return px value.
     */
    public static int dpToPx(@NonNull Resources res, int dp) {
        return (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
    }
    
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        return px / (metrics.densityDpi / 160f);
    }

    public static String getUppercaseLetters(String in) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> queryPairs = new HashMap<>();
        String query = url.getQuery();
        if (query == null) {
            return queryPairs;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return queryPairs;
    }

    public static String toHumanTimePeriod(Context ctx, DateTime start, DateTime end) {
        String result;
        Resources res = ctx.getResources();
        Period p = new Period(start, end);

        if (p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0 && p.getDays() == 0 && p.getHours() == 0 && p.getMinutes() == 0) {
            result = res.getQuantityString(R.plurals.seconds_ago, p.getSeconds(), p.getSeconds());
        } else if (p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0 && p.getDays() == 0 && p.getHours() == 0) {
            result = res.getQuantityString(R.plurals.minutes_ago, p.getMinutes(), p.getMinutes());
        } else if (p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0 && p.getDays() == 0) {
            result = res.getQuantityString(R.plurals.hours_ago, p.getHours(), p.getHours());
        } else if (p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0) {
            result = res.getQuantityString(R.plurals.days_ago, p.getDays(), p.getDays());
        } else {
            result = start.toLocalDateTime().toString(DateTimeFormat.patternForStyle("M-", res.getConfiguration().locale));
        }
        return result;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager mConMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return mConMgr.getActiveNetworkInfo() != null
                && mConMgr.getActiveNetworkInfo().isAvailable()
                && mConMgr.getActiveNetworkInfo().isConnected();
    }

    public static boolean isEmulator() {
        Timber.d(Build.PRODUCT);
        return Build.PRODUCT.equals("google_sdk");
    }

    public static long stringToLong(String str) {
        long l = 0;
        for (int i = 0; i < str.length(); i++) {
            l += str.charAt(i) * Math.pow(10, i);
        }
        return l;
    }

    public static String inputStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        is.close();

        return sb.toString();
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .create();
    }

    public static Gson getGson(FieldNamingPolicy policy) {
        return new GsonBuilder()
                .setFieldNamingPolicy(policy)
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .create();
    }

    public static Gson getGson(FieldNamingPolicy policy, JsonDeserializer<DateTime> dateTimeDeserializer) {
        return new GsonBuilder()
                .setFieldNamingPolicy(policy)
                .registerTypeAdapter(DateTime.class, dateTimeDeserializer)
                .create();

    }

    /**
     * Utility function to check if the provided String is an email or not.
     *
     * @param possibleEmail Given String.
     * @return true if the given String is an email address.
     */
    public static boolean isEmailAddress(String possibleEmail) {
        return Patterns.EMAIL_ADDRESS.matcher(possibleEmail).matches();
    }

    public static boolean canLaunch(Context context, final Intent viewUrlIntent) {
        return context.getPackageManager().resolveActivity(viewUrlIntent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    @SuppressWarnings("deprecation")
    public static Intent createExternalIntent(Context context, Uri uri) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setToolbarColor(context.getResources().getColor(R.color.theme_primary))
                .setShowTitle(true)
                .build();
        Intent intent = customTabsIntent.intent;
        intent.setData(uri);
        return intent;
    }
}
