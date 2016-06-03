package org.gdg.frisbee.android.arrow;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class NotificationHandler {

    private final Context context;
    public static final DateTime SUMMIT_DATE_TIME = new DateTime(2016, 5, 17, 15, 0, DateTimeZone.UTC);

    public NotificationHandler(Context context) {
        this.context = context;
    }

    public boolean shouldSetAlarm() {
        return !PrefUtils.isSummitNotificationSent(context)
            && SUMMIT_DATE_TIME.isAfterNow();
    }

    public void setAlarmForNotification() {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(SummitNotificationReceiver.ACTION_SUMMIT_NOTIFICATION);

        // Tuesday 8 AM PST in the morning
        am.set(AlarmManager.RTC_WAKEUP, SUMMIT_DATE_TIME.getMillis(),
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public Notification createNotification() {

        Intent mainIntent = new Intent(context, MainActivity.class);
        Intent arrowIntent = new Intent(context, ArrowActivity.class);

        CharSequence message = context.getString(R.string.message_io_greeting);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setContentTitle(context.getString(R.string.title_io_greeting))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_drawer_devfest)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .addAction(R.drawable.ic_drawer_arrow, "Play",
                PendingIntent.getActivity(context, 0, arrowIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            .setContentIntent(PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        return builder.build();
    }
}
