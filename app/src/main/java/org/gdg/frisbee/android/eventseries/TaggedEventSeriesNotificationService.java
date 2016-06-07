package org.gdg.frisbee.android.eventseries;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.app.OrganizerChecker;

public class TaggedEventSeriesNotificationService extends Service
    implements
    GoogleApiClient.ConnectionCallbacks,
    OrganizerChecker.Callbacks,
    GoogleApiClient.OnConnectionFailedListener {

    public static final int NOTIFICATION_ID = 1;
    private GoogleApiClient apiClient;
    private Intent intent;

    public TaggedEventSeriesNotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        apiClient = GoogleApiClientFactory.createWith(this);
        apiClient.registerConnectionCallbacks(this);
        apiClient.registerConnectionFailedListener(this);
        apiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        apiClient.unregisterConnectionCallbacks(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.intent = intent;
        return START_STICKY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        App.getInstance().checkOrganizer(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        stop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stop();
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
