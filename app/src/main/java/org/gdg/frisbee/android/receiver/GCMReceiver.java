package org.gdg.frisbee.android.receiver;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.gdg.frisbee.android.service.GcmIntentService;
import org.gdg.frisbee.android.utils.PrefUtils;

public class GCMReceiver extends WakefulBroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (PrefUtils.isGcmEnabled(context)) {
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GcmIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, intent.setComponent(comp));
        }
        setResultCode(Activity.RESULT_OK);
    }
}
