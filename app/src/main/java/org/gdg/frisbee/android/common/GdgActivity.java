/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under 
 * * the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.common;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import java.util.List;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.achievements.AchievementActionHandler;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.RecentTasksStyler;
import org.gdg.frisbee.android.utils.Utils;

import butterknife.ButterKnife;

public abstract class GdgActivity extends TrackableActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int RC_SIGN_IN = 0;
    private static final int DIALOG_PLAY_SERVICES_ERROR = 0;
    private AchievementActionHandler mAchievementActionHandler;
    private Handler mHandler = new Handler();

    // GoogleApiClient wraps our service connection to Google Play services and
    // provides access to the users sign in state and Google's APIs.
    private GoogleApiClient mGoogleApiClient;

    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //       STATE_DEFAULT: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //   STATE_IN_PROGRESS: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    private int mSignInProgress;

    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent mSignInIntent;

    private Toolbar mActionBarToolbar;

    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        ButterKnife.bind(this);
        getActionBarToolbar();
    }

    @Override
    protected String getTrackedViewName() {
        return null;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecentTasksStyler.styleRecentTasksEntry(this);

        mGoogleApiClient = createGoogleApiClient();

        mAchievementActionHandler =
                new AchievementActionHandler(getHandler(), mGoogleApiClient, this);
    }

    protected GoogleApiClient createGoogleApiClient() {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(AppStateManager.API);
        if (PrefUtils.isSignedIn(this)) {
            builder.addApiIfAvailable(Plus.API, Plus.SCOPE_PLUS_LOGIN, Plus.SCOPE_PLUS_PROFILE)
                    .addApiIfAvailable(Games.API, Games.SCOPE_GAMES);
        } else {
            builder.addApiIfAvailable(Plus.API)
                    .addApiIfAvailable(Games.API);
        }
        return builder.build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.registerConnectionFailedListener(this);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected AchievementActionHandler getAchievementActionHandler() {
        return mAchievementActionHandler;
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        if (PrefUtils.isSignedIn(this) && !Utils.isEmulator()) {
            switch (requestCode) {
                case RC_SIGN_IN:
                    if (responseCode == RESULT_OK) {
                        // If the error resolution was successful we should continue
                        // processing errors.
                        mSignInProgress = STATE_SIGN_IN;
                        App.getInstance().resetOrganizer();

                        if (!mGoogleApiClient.isConnecting()) {
                            // If Google Play services resolved the issue with a dialog then
                            // onStart is not called so we need to re-attempt connection here.
                            mGoogleApiClient.connect();
                        }
                    } else {
                        // If the error resolution was not successful or the user canceled,
                        // we should stop processing errors.
                        mSignInProgress = STATE_DEFAULT;
                    }
                    break;
            }
        }
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                mActionBarToolbar.setNavigationIcon(R.drawable.ic_drawer);
                setSupportActionBar(mActionBarToolbar);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        return mActionBarToolbar;
    }

    private void resolveSignInError() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            showDialog(DIALOG_PLAY_SERVICES_ERROR);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isLastActivityOnStack()) {
            overridePendingTransition(0, 0);
        }
    }

    private boolean isLastActivityOnStack() {
        ActivityManager mngr = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);

        return taskList.get(0).numActivities == 1
                && taskList.get(0).topActivity.getClassName().equals(this.getClass().getName());
    }

    @Override
    public void onConnected(Bundle bundle) {
        mAchievementActionHandler.onConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mSignInProgress != STATE_IN_PROGRESS) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();

            //if (mSignInProgress == STATE_SIGN_IN) {
            // STATE_SIGN_IN indicates the user already clicked the sign in button
            // so we should continue processing errors until the user is signed in
            // or they click cancel.
            resolveSignInError();
            //}
        }
    }

    public void setToolbarTitle(final String title) {
        getActionBarToolbar().setTitle(title);
    }
}
