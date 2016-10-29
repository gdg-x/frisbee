package org.gdg.frisbee.android.eventseries;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.OrganizerChecker;

public class TaggedEventSeriesNotificationService extends Service implements OrganizerChecker.Callbacks {

    private static final int NOTIFICATION_ID = 1;
    private Intent intent;

    public TaggedEventSeriesNotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.from(this).checkOrganizer(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        return START_STICKY;
    }

    @Override
    public void onOrganizerResponse(boolean isOrganizer) {
        if (isOrganizer || intent.getBooleanExtra(Const.EXTRA_ALARM_FOR_ALL, true)) {
            TaggedEventSeries eventSeries = intent.getParcelableExtra(Const.EXTRA_TAGGED_EVENT);
            NotificationHandler notificationHandler = new NotificationHandler(this, eventSeries);
            Notification notification = notificationHandler.createNotification();

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);
        }

        stop();
    }

    @Override
    public void onErrorResponse() {
        stop();
    }

    private void stop() {
        stopSelf();
        NotificationReceiver.completeWakefulIntent(intent);
    }
}
