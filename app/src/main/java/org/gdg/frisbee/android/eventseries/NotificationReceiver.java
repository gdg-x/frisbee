package org.gdg.frisbee.android.eventseries;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class NotificationReceiver extends WakefulBroadcastReceiver {
    public static final String ACTION_SUMMIT_NOTIFICATION = "org.gdg.frisbee.android.ACTION_SUMMIT_NOTIFICATION";

    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, TaggedEventSeriesNotificationService.class);
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
