package org.gdg.frisbee.android.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.event.EventActivity;
import org.gdg.frisbee.android.receiver.GCMReceiver;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import timber.log.Timber;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 12.09.13
 * Time: 01:07
 * To change this template use File | Settings | File Templates.
 */
public class GcmIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    public static final String LOG_TAG = "GDG-GcmIntentService";
    public static final String UPCOMING_EVENT = "upcoming_event";
    public static final String EXTRA_TYPE = "type";

    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                Timber.i("Received: " + extras.toString());

                if(UPCOMING_EVENT.equals(extras.getString(EXTRA_TYPE))) {
                    sendEventNotification(extras);
                }
            }
        }

        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMReceiver.completeWakefulIntent(intent);
    }

    private void sendEventNotification(Bundle extras) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent eventIntent = new Intent();
        App.getInstance().getTracker().sendEvent("gcm","clickedGcmEventNotification",extras.getString("id"), 0L);
        eventIntent.setClass(getApplicationContext(), EventActivity.class);
        eventIntent.putExtra(Const.EXTRA_GROUP_ID, extras.getString("chapter"));
        eventIntent.putExtra(Const.EXTRA_EVENT_ID, extras.getString("id"));
        eventIntent.putExtra(Const.EXTRA_SECTION, EventActivity.EventPagerAdapter.SECTION_OVERVIEW);

        final DateTimeFormatter df = DateTimeFormat
                .forPattern("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");

        DateTime start = df.parseDateTime(extras.getString("start"));
        DateTime end = df.parseDateTime(extras.getString("end"));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, eventIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_gdg)
                        .setContentTitle(extras.containsKey("title") ? extras.getString("title") : "")
                        .setAutoCancel(true)
                        .setTicker(getString(R.string.gcm_event_ticker))
                        .setLights(0x0000FF00, 5000, 5000)
                        .setOnlyAlertOnce(true)
                        .setSound(alarmSound, AudioManager.STREAM_NOTIFICATION)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(start.toLocalDateTime().toString(DateTimeFormat.patternForStyle("MS",getResources().getConfiguration().locale))))
                        .setContentText(start.toLocalDateTime().toString(DateTimeFormat.patternForStyle("MS",getResources().getConfiguration().locale)));

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
