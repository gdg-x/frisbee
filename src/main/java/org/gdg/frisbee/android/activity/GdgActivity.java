/*
 * Copyright 2013 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusClient;
import org.gdg.frisbee.android.utils.ScopedBus;
import org.gdg.frisbee.android.utils.Utils;

import java.io.IOException;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 21.04.13
 * Time: 21:56
 */
public abstract class GdgActivity extends RoboSherlockFragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener{

    private static final String LOG_TAG = "GDG-GdgActivity";
    private static final int REQUEST_CODE_RESOLVE_ERR = 7;

    private PlusClient mPlusClient;
    private ConnectionResult mConnectionResult;

    private final ScopedBus scopedBus = new ScopedBus();

    protected ScopedBus getBus() {
        return scopedBus;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!Utils.isEmulator()) {
            int errorCode = GooglePlusUtil.checkGooglePlusApp(this);
            if (errorCode != GooglePlusUtil.SUCCESS) {
                GooglePlusUtil.getErrorDialog(errorCode, this, 0).show();
            } else {
                mPlusClient = new PlusClient.Builder(this, this, this)
                    .setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
                    .build();
            }
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(mPlusClient != null && !mPlusClient.isConnected()) {
            if (mConnectionResult == null) {
                mPlusClient.connect();
            } else {
                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    // Try connecting again.
                    mConnectionResult = null;
                    mPlusClient.connect();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
        if(mPlusClient != null && !mPlusClient.isConnected()) {
            if (mConnectionResult == null) {
                mPlusClient.connect();
            } else {
                try {
                    mConnectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
                } catch (IntentSender.SendIntentException e) {
                    // Try connecting again.
                    mConnectionResult = null;
                    mPlusClient.connect();
                }
            }
        }   */
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mPlusClient != null && mPlusClient.isConnected())
            mPlusClient.disconnect();
    }

    public PlusClient getPlusClient() {
        return mPlusClient;
    }

    @Override
    protected void onPause() {
        super.onPause();
        getBus().paused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBus().resumed();
    }

    @Override
    public void onConnected() {
        Log.d(LOG_TAG, "onConnected()");
        final String accountName = mPlusClient.getAccountName();
    }

    @Override
    public void onDisconnected() {
        Log.d(LOG_TAG, "onDisconnected()");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed()");
        Log.d(LOG_TAG, "Connection failed: "+ connectionResult.getErrorCode());

        if (connectionResult.hasResolution()) {
            try {
                Log.v(LOG_TAG, "resolve");
                connectionResult.startResolutionForResult(GdgActivity.this, REQUEST_CODE_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                Log.e(LOG_TAG, "send intent",e);
                mConnectionResult = null;
                mPlusClient.connect();
            }
        } else {
            Log.d(LOG_TAG, "no resolution!?");
        }
        // Save the result and resolve the connection failure upon a user click.
        mConnectionResult = connectionResult;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && responseCode == RESULT_OK) {
            mConnectionResult = null;
            mPlusClient.connect();

        }
    }
}