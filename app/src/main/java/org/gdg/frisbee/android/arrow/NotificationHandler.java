package org.gdg.frisbee.android.arrow;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.gdg.frisbee.android.eventseries.TaggedEventSeriesActivity;
import org.gdg.frisbee.android.utils.PrefUtils;

public class NotificationHandler {

    private final Context context;
    private final TaggedEventSeries eventSeries;

    public NotificationHandler(Context context, TaggedEventSeries eventSeries) {
        this.context = context;
        this.eventSeries = eventSeries;
    }

    public boolean shouldSetAlarm() {
        return !PrefUtils.isTaggedEventSeriesNotificationSet(context, eventSeries)
            && eventSeries.getStartDateInMillis().isAfterNow();
    }

    public void setAlarmForNotification() {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(SummitNotificationReceiver.ACTION_SUMMIT_NOTIFICATION);
        intent.putExtra(Const.EXTRA_TAGGED_EVENT, eventSeries);

        am.set(AlarmManager.RTC_WAKEUP, eventSeries.getStartDateInMillis().getMillis(),
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public Notification createNotification() {

        Intent contentIntent = new Intent(context, TaggedEventSeriesActivity.class);
        contentIntent.putExtra(Const.EXTRA_TAGGED_EVENT, eventSeries);

        String message = context.getString(eventSeries.getGreetingsResId());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
            .setContentTitle(context.getString(eventSeries.getGreetingsTitleResId()))
            .setContentText(message)
            .setSmallIcon(eventSeries.getDrawerIconResId())
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        return builder.build();
    }
}
