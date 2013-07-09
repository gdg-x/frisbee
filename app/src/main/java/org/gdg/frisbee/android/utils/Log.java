package org.gdg.frisbee.android.utils;

import org.gdg.frisbee.android.Const;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 08.07.13
 * Time: 17:59
 * To change this template use File | Settings | File Templates.
 */
public class Log {

    public static void d(String tag, String msg) {
        if(Const.LOG_LEVEL == Const.LogLevel.DEBUG)
            android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if(Const.LOG_LEVEL >= Const.LogLevel.ERROR)
            android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg, Throwable tr) {
        if(Const.LOG_LEVEL >= Const.LogLevel.ERROR)
            android.util.Log.d(tag, msg, tr);
    }
}
