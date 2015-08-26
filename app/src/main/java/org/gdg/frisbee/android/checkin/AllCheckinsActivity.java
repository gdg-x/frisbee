package org.gdg.frisbee.android.checkin;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import org.gdg.frisbee.android.common.GdgActivity;

public class AllCheckinsActivity extends GdgActivity {
    private static final String TAG = AllCheckinsActivity.class.getSimpleName();
    private static final int REQUEST_RESOLVE_ERROR = 1;
    public static final String CHECKIN_TYPE = "CHECKIN";

    private MessageListener mMessageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            if (CHECKIN_TYPE.equals(message.getNamespace())) {
                Log.i(TAG, "received message: " + new String(message.getContent()));
            }
        }
    };
    
    private boolean mResolvingError;

    @Override
    public void onConnected(Bundle bundle) {
        Nearby.Messages.getPermissionStatus(getGoogleApiClient()).setResultCallback(
                new ErrorCheckingCallback("getPermissionStatus", new Runnable() {
                    @Override
                    public void run() {
                        subscribeForCheckins();
                    }
                })
        );

        
        super.onConnected(bundle);
    }

    @Override
    protected void onStop() {
        if (getGoogleApiClient().isConnected()) {
            Nearby.Messages.unsubscribe(getGoogleApiClient(), mMessageListener)
                    .setResultCallback(new ErrorCheckingCallback("unsubscribe"));
        }
        super.onStop();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                subscribeForCheckins();
            } else {
                // This may mean that user had rejected to grant nearby permission.
                Log.e(TAG, "Failed to resolve error with code " + resultCode);
            }
        }
    }

    private void subscribeForCheckins() {
        Nearby.Messages.subscribe(getGoogleApiClient(), mMessageListener)
                .setResultCallback(new ErrorCheckingCallback("subscribe()"));
    }


    private class ErrorCheckingCallback implements ResultCallback<Status> {
        private final String method;
        private final Runnable runOnSuccess;

        private ErrorCheckingCallback(String method) {
            this(method, null);
        }

        private ErrorCheckingCallback(String method, @Nullable Runnable runOnSuccess) {
            this.method = method;
            this.runOnSuccess = runOnSuccess;
        }

        @Override
        public void onResult(@NonNull Status status) {
            if (status.isSuccess()) {
                Log.i(TAG, method + " succeeded.");
                if (runOnSuccess != null) {
                    runOnSuccess.run();
                }
            } else {
                // Currently, the only resolvable error is that the device is not opted
                // in to Nearby. Starting the resolution displays an opt-in dialog.
                if (status.hasResolution()) {
                    if (!mResolvingError) {
                        try {
                            status.startResolutionForResult(AllCheckinsActivity.this,
                                    REQUEST_RESOLVE_ERROR);
                            mResolvingError = true;
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, method + " failed with exception: " + e);
                        }
                    } else {
                        // This will be encountered on initial startup because we do
                        // both publish and subscribe together.  So having a toast while
                        // resolving dialog is in progress is confusing, so just log it.
                        Log.i(TAG, method + " failed with status: " + status
                                + " while resolving error.");
                    }
                } else {
                    Log.e(TAG, method + " failed with : " + status
                            + " resolving error: " + mResolvingError);
                }
            }
        }
    }
}
