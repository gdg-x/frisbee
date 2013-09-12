package org.gdg.frisbee.android.receiver;

import android.app.Activity;
import android.content.*;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.service.GcmIntentService;
import org.gdg.frisbee.android.utils.WakefulBroadcastReceiver;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 05.07.13
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public class GCMReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("gdg",Context.MODE_PRIVATE);

        if(preferences.getBoolean(Const.SETTINGS_GCM, false)) {
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GcmIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
        }
        setResultCode(Activity.RESULT_OK);
    }
}
