package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * Copied from internal package from AppCompat
 * {@code android.support.v7.internal.widget.ThemeUtils}
 *
 */
public class ThemeUtils {

    private ThemeUtils() {
        // Prevent instances of this class being created.
    }

    private static final int[] TEMP_ARRAY = new int[1];

    public static int getThemeAttrColor(Context context, int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }
}
