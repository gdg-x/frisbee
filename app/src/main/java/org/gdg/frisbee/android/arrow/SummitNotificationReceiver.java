package org.gdg.frisbee.android.arrow;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.eventseries.TaggedEventSeriesNotificationService;

public class SummitNotificationReceiver extends WakefulBroadcastReceiver {
    public static final String ACTION_SUMMIT_NOTIFICATION = "org.gdg.frisbee.android.ACTION_SUMMIT_NOTIFICATION";

    public SummitNotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, TaggedEventSeriesNotificationService.class);
        serviceIntent.putExtra(Const.EXTRA_TAGGED_EVENT, intent.getParcelableExtra(Const.EXTRA_TAGGED_EVENT));
        serviceIntent.putExtras(intent);
        startWakefulService(context, serviceIntent);
    }
}
