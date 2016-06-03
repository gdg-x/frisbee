package org.gdg.frisbee.android.arrow;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class SummitNotificationReceiver extends WakefulBroadcastReceiver {
    public static final String ACTION_SUMMIT_NOTIFICATION = "org.gdg.frisbee.android.ACTION_SUMMIT_NOTIFICATION";

    public SummitNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, SummitNotificationService.class);
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
