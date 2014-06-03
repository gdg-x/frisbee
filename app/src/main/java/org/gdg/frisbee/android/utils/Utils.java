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

package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.utils
 * <p/>
 * User: maui
 * Date: 21.04.13
 * Time: 22:34
 */
public class Utils {
    public static <T> List<T> createListOfType(Class<T> type) {
        return new ArrayList<T>();
    }

    public static <K,V> Map<K,V> createMapOfType(Class<K> keyType, Class<V> valueType) {
        return new HashMap<K, V>();
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

    public static float convertPixelsToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static String getUppercaseLetters(String in ) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if(Character.isUpperCase(c)) sb.append(c);
        }
        return sb.toString();
    }

    public static Map<String, String> splitQuery(URL url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new HashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    public static String toHumanTimePeriod(Context ctx, DateTime start, DateTime end) {
        String result = "";
        Resources res = ctx.getResources();
        Period p = new Period(start, end);

        if(p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0 && p.getDays() == 0 && p.getHours() == 0 && p.getMinutes() == 0) {
            result = res.getQuantityString(R.plurals.seconds_ago, p.getSeconds(), p.getSeconds());
        } else if(p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0 && p.getDays() == 0 && p.getHours() == 0) {
            result = res.getQuantityString(R.plurals.minutes_ago, p.getMinutes(), p.getMinutes());
        } else if(p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0 && p.getDays() == 0) {
            result = res.getQuantityString(R.plurals.hours_ago, p.getHours(), p.getHours());
        } else if(p.getYears() == 0 && p.getMonths() == 0 && p.getWeeks() == 0){
            result = res.getQuantityString(R.plurals.days_ago, p.getDays(), p.getDays());
        } else {
            result = start.toLocalDateTime().toString(DateTimeFormat.patternForStyle("M-", res.getConfiguration().locale));
        }
        return result;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager mConMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (mConMgr.getActiveNetworkInfo() != null
                && mConMgr.getActiveNetworkInfo().isAvailable()
                && mConMgr.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEmulator() {
        Log.d("GDG", Build.PRODUCT);
        return Build.PRODUCT.equals("google_sdk");
    }

    public static long stringToLong(String str) {
        long l = 0;
        for(int i = 0; i < str.length(); i++) {
            l += str.charAt(i)*Math.pow(10, i);
        }
        return l;
    }

    public static String inputStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        is.close();

        return sb.toString();
    }

    public static ArrayList<View> findViewByType(ViewGroup root, Class clazz) {
        ArrayList<View> views = new ArrayList<View>();
        int count = root.getChildCount();
        for (int i = 0; i <= count; i++) {
            View v = root.getChildAt(i);
            if(v != null) {
                if (v.getClass().equals(clazz)) {
                    views.add(v);
                } else if(v instanceof ViewGroup) {
                    views.addAll(findViewByType((ViewGroup)v, clazz));
                }
            }
        }
        return views;
    }
}
