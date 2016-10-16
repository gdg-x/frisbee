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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.RecentTasksStyler;
import org.gdg.frisbee.android.view.ColoredSnackBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public abstract class GdgActivity extends TrackableActivity implements
    GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 101;

    @Nullable
    @BindView(R.id.content_frame)
    FrameLayout mContentLayout;

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiClient signInClient;

    private Toolbar mActionBarToolbar;

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
        if (PrefUtils.isSignedIn(this) && PlusUtils.getCurrentAccount(this) == null) {
            requestSignIn();
        }
    }

    /**
     * Create {@link GoogleApiClient}. This can be overridden to change the scope of the GoogleApiClient
     *
     * @return {@link GoogleApiClient} without connecting. {@code connect()} must be called afterwards.
     */
    protected GoogleApiClient createGoogleApiClient() {
        return GoogleApiClientFactory.createWith(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);
        mGoogleApiClient.disconnect();
    }

    protected void requestSignIn() {
        if (signInClient == null) {
            signInClient = GoogleApiClientFactory.createForSignIn(this, this);
        }
        Auth.GoogleSignInApi.silentSignIn(signInClient).setResultCallback(new ResultCallbacks<GoogleSignInResult>() {
            @Override
            public void onSuccess(@NonNull GoogleSignInResult result) {
                onSuccessfulSignIn(result.getSignInAccount());
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(signInClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                onSuccessfulSignIn(result.getSignInAccount());
            } else {
                PrefUtils.setSignedOut(this);
            }
        }
    }

    /**
     * Called when a successful SignIn operation is made
     *
     * @param signInAccount {@link GoogleSignInAccount} of the user
     */
    @CallSuper
    protected void onSuccessfulSignIn(GoogleSignInAccount signInAccount) {
        PrefUtils.setSignedIn(this);
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

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        PendingIntent mSignInIntent = result.getResolution();
        if (mSignInIntent == null && PrefUtils.shouldShowFatalPlayServiceMessage(this)) {
            showFatalPlayServiceMessage(result);
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
