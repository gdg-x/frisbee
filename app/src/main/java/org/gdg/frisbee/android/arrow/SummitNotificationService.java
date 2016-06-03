package org.gdg.frisbee.android.arrow;

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

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.app.OrganizerChecker;
import org.gdg.frisbee.android.utils.PrefUtils;

public class SummitNotificationService extends Service
    implements
    GoogleApiClient.ConnectionCallbacks,
    OrganizerChecker.Callbacks,
    GoogleApiClient.OnConnectionFailedListener {

    public static final int NOTIFICATION_ID = 1;
    private GoogleApiClient apiClient;
    private Intent intent;
    private NotificationHandler notificationHandler;

    public SummitNotificationService() {
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

        notificationHandler = new NotificationHandler(this);
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
        if (isOrganizer) {
            Notification notification = notificationHandler.createNotification();

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(NOTIFICATION_ID, notification);

            PrefUtils.setSummitNotificationSent(this);
        }

        stop();
    }

    @Override
    public void onErrorResponse() {
        stop();
    }

    private void stop() {
        stopSelf();
        SummitNotificationReceiver.completeWakefulIntent(intent);
    }
}
