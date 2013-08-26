package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.gdg.frisbee.android.Const;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 21.08.13
 * Time: 22:36
 * To change this template use File | Settings | File Templates.
 */
public class Gcm {

    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = context.getSharedPreferences("gdg", Context.MODE_PRIVATE);
        String registrationId = prefs.getString(Const.SETTINGS_GCM_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        return registrationId;
    }
}
