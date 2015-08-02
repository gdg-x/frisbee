package org.gdg.frisbee.android.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.internal.widget.ThemeUtils;

import org.gdg.frisbee.android.R;

/**
 * Helper class that applies the proper icon, title and background color to recent tasks list.
 */
public class RecentTasksStyler {
    private static Bitmap sIcon = null;

    private RecentTasksStyler() {
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void styleRecentTasksEntry(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        Resources resources = activity.getResources();
        String label = resources.getString(activity.getApplicationInfo().labelRes);

        if (sIcon == null) {
            // Cache to avoid decoding the same bitmap on every Activity change
            sIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_drawer_devfest);
        }

        int colorPrimary = ThemeUtils.getThemeAttrColor(activity, R.attr.colorPrimary);
        activity.setTaskDescription(new ActivityManager.TaskDescription(label, sIcon, colorPrimary));
    }
}
