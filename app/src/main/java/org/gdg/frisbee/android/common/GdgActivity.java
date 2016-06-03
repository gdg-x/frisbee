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

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.achievements.AchievementActionHandler;
import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.arrow.NotificationHandler;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.RecentTasksStyler;
import org.gdg.frisbee.android.view.ColoredSnackBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public abstract class GdgActivity extends TrackableActivity implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int RC_SIGN_IN = 101;
    private static final String SAVED_PROGRESS = "SAVED_PROGRESS";
    @Nullable
    @BindView(R.id.content_frame)
    FrameLayout mContentLayout;
    private AchievementActionHandler mAchievementActionHandler;

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
    private boolean isSignedIn;

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

    public AchievementActionHandler getAchievementActionHandler() {
        return mAchievementActionHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecentTasksStyler.styleRecentTasksEntry(this);

        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState.getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }

        mGoogleApiClient = createGoogleApiClient();

        mAchievementActionHandler =
            new AchievementActionHandler(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        recreateGoogleApiClientIfNeeded();
        mGoogleApiClient.connect();
    }

    protected final void recreateGoogleApiClientIfNeeded() {
        if (isSignedIn != PrefUtils.isSignedIn(this)) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);
            mGoogleApiClient.disconnect();

            mGoogleApiClient = createGoogleApiClient();
        }

        if (!mGoogleApiClient.isConnectionCallbacksRegistered(this)) {
            mGoogleApiClient.registerConnectionCallbacks(this);
        }
        if (!mGoogleApiClient.isConnectionFailedListenerRegistered(this)) {
            mGoogleApiClient.registerConnectionFailedListener(this);
        }
    }

    /**
     * Create {@link GoogleApiClient}. This can be overridden to change the scope of the GoogleApiClient
     *
     * @return {@link GoogleApiClient} without connecting. {@code connect()} must be called afterwards.
     */
    protected GoogleApiClient createGoogleApiClient() {
        isSignedIn = PrefUtils.isSignedIn(this);
        return GoogleApiClientFactory.createWith(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        if (PrefUtils.isSignedIn(this)) {
            switch (requestCode) {
                case RC_SIGN_IN:
                    if (responseCode == RESULT_OK) {
                        // If the error resolution was successful we should continue
                        // processing errors.
                        mSignInProgress = STATE_SIGN_IN;
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
            startIntentSenderForResult(
                mSignInIntent.getIntentSender(),
                RC_SIGN_IN, null, 0, 0, 0
            );
        } catch (IntentSender.SendIntentException e) {
            // The intent was canceled before it was sent.  Attempt to connect to
            // get an updated ConnectionResult.
            mSignInProgress = STATE_SIGN_IN;
            mGoogleApiClient.connect();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean isContextValid() {
        boolean isContextValid = !isFinishing()
            && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !isDestroyed());
        if (!isContextValid) {
            Timber.d("Context is not valid");
        }
        return isContextValid;
    }

    protected void showError(@StringRes final int errorStringRes) {
        if (isContextValid()) {
            if (mContentLayout != null) {
                Snackbar snackbar = Snackbar.make(mContentLayout, errorStringRes,
                    Snackbar.LENGTH_SHORT);
                ColoredSnackBar.alert(snackbar).show();
            } else {
                Toast.makeText(this, errorStringRes, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mAchievementActionHandler.onConnected();

        initSummitNotificationHandler();
    }

    private void initSummitNotificationHandler() {
        NotificationHandler notificationHandler = new NotificationHandler(this);
        if (notificationHandler.shouldSetAlarm()) {
            notificationHandler.setAlarmForNotification();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (mSignInProgress != STATE_IN_PROGRESS) {

            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();
            if (mSignInIntent != null) {
                resolveSignInError();
            } else {
                // Google Play services wasn't able to provide an intent for some
                // error types, so we show the default Google Play services error
                // dialog which may still start an intent on our behalf if the
                // user can resolve the issue.
                if (PrefUtils.shouldShowFatalPlayServiceMessage(this)) {
                    showFatalPlayServiceMessage(result);
                }
            }
        }
    }

    private void showFatalPlayServiceMessage(@NonNull ConnectionResult result) {
        Timber.e("Google Play Service did not resolve error");
        GoogleApiAvailability.getInstance().showErrorNotification(this, result.getErrorCode());
        PrefUtils.setFatalPlayServiceMessageShown(this);
    }

    public void setToolbarTitle(final String title) {
        getActionBarToolbar().setTitle(title);
    }

    protected void recordEndPageView(Action viewAction) {
        PendingResult<Status> result = AppIndex.AppIndexApi.end(getGoogleApiClient(), viewAction);
        result.setResultCallback(appIndexApiCallback("end " + viewAction));
    }

    protected void recordStartPageView(Action viewAction) {
        PendingResult<Status> result = AppIndex.AppIndexApi.start(getGoogleApiClient(), viewAction);
        result.setResultCallback(appIndexApiCallback("start " + viewAction));
    }

    protected ResultCallback<Status> appIndexApiCallback(final String label) {
        return new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Timber.d("App Indexing API: Recorded event %s view successfully.", label);
                } else {
                    Timber.e("App Indexing API: There was an error recording the event view. Status = %s",
                        status.toString());
                }
            }
        };
    }
}
