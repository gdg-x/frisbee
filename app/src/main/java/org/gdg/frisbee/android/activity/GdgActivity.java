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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.plus.GooglePlusUtil;
import com.google.android.gms.plus.PlusClient;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
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
public abstract class GdgActivity extends RoboSherlockFragmentActivity implements PlayServicesHelper.PlayServicesHelperListener {

    private static final String LOG_TAG = "GDG-GdgActivity";
    private static final int REQUEST_CODE_RESOLVE_ERR = 7;

    private PlayServicesHelper mPlayServicesHelper;

    private ConnectionResult mConnectionResult;
    private SharedPreferences mPreferences;
    private Handler mHandler = new Handler();
    private final ScopedBus scopedBus = new ScopedBus();

    protected ScopedBus getBus() {
        return scopedBus;
    }

    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = getSharedPreferences("gdg",MODE_PRIVATE);

        if(!Utils.isEmulator()) {
            int errorCode = GooglePlusUtil.checkGooglePlusApp(this);
            if (errorCode != GooglePlusUtil.SUCCESS) {
                GooglePlusUtil.getErrorDialog(errorCode, this, 0).show();
            } else {

                if(mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
                    initPlayServices();
                }
            }
        }

        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    protected void initPlayServices() {
        mPlayServicesHelper = new PlayServicesHelper(this);
        mPlayServicesHelper.setup(this, PlayServicesHelper.CLIENT_PLUS | PlayServicesHelper.CLIENT_GAMES);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
            mPlayServicesHelper.onStart(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
            mPlayServicesHelper.onStop();
        }
    }

    public PlayServicesHelper getPlayServicesHelper() {
        return mPlayServicesHelper;
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
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        if(mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
            mPlayServicesHelper.onActivityResult(requestCode,responseCode,intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    @Override
    public void onSignInFailed() {
        Log.d(LOG_TAG, "onSignInFailed");
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(LOG_TAG, "onSignInSucceeded");
    }
}