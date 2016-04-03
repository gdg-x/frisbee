package org.gdg.frisbee.android;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class GdgWatchFaceConfigListenerService extends WearableListenerService
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WatchFaceConfigService";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        if (!messageEvent.getPath().equals(WearableConfigurationUtil.PATH_ANALOG)) {
            return;
        }

        byte[] rawData = messageEvent.getData();
        DataMap configKeys = DataMap.fromByteArray(rawData);
        Timber.d("configKeys = [" + configKeys + "]");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Wearable.API).build();
        }
        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult =
                mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
                return;
            }
        }

        WearableConfigurationUtil.updateKeysInConfigDataMap(mGoogleApiClient, messageEvent.getPath(), configKeys);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("onConnected: " + bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Timber.d("onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("onConnectionFailed: " + connectionResult);
    }
}
