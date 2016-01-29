/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.common.GdgActivity;

import timber.log.Timber;

/**
 * Activity for displaying information about a receive App Invite invitation.  This activity
 * displays as a Dialog over the MainActivity and does not cover the full screen.
 */
public class AppInviteDeepLinkActivity extends GdgActivity {

    // Invitation intent received while GoogleApiClient was not connected, to be reported
    // on connection
    private Intent mCachedInvitationIntent;

    @Override
    protected GoogleApiClient createGoogleApiClient() {
        return GoogleApiClientFactory.createWithoutSignIn(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final AppCompatDialogFragment dialogFragment = new AppCompatDialogFragment() {

            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return new AlertDialog.Builder(AppInviteDeepLinkActivity.this)
                        .setTitle("Congrats!")
                        .setMessage("You installed the app with an invite. Here is your reward!")
                        .create();
            }
        };
        dialogFragment.show(
                getSupportFragmentManager(),
                AppInviteDeepLinkActivity.class.getSimpleName()
        );
    }

    // [START deep_link_on_start]
    @Override
    protected void onStart() {
        super.onStart();

        // If app is already installed app and launched with deep link that matches
        // AppInviteDeepLinkActivity filter, then the referral info will be in the intent
        Intent intent = getIntent();
        processReferralIntent(intent);
    }
    // [END deep_link_on_start]

    // [START process_referral_intent]
    private void processReferralIntent(Intent intent) {
        if (!AppInviteReferral.hasReferral(intent)) {
            Timber.e("Error: AppInviteDeepLinkActivity Intent does not contain App Invite");
            return;
        }

        // Extract referral information from the intent
        String invitationId = AppInviteReferral.getInvitationId(intent);
        String deepLink = AppInviteReferral.getDeepLink(intent);

        Timber.d("Found Referral: " + invitationId + ":" + deepLink);

        if (getGoogleApiClient().isConnected()) {
            // Notify the API of the install success and invitation conversion
            updateInvitationStatus(intent);
        } else {
            // Cache the invitation ID so that we can call the AppInvite API after
            // the GoogleAPIClient connects
            Timber.w("Warning: GoogleAPIClient not connected, can't update invitation.");
            mCachedInvitationIntent = intent;
        }
    }
    // [END process_referral_intent]

    /** Update the install and conversion status of an invite intent **/
    // [START update_invitation_status]
    private void updateInvitationStatus(Intent intent) {
        String invitationId = AppInviteReferral.getInvitationId(intent);

        // Note: these  calls return PendingResult(s), so one could also wait to see
        // if this succeeds instead of using fire-and-forget, as is shown here
        if (AppInviteReferral.isOpenedFromPlayStore(intent)) {
            AppInvite.AppInviteApi.updateInvitationOnInstall(getGoogleApiClient(), invitationId);
        }

        // If your invitation contains deep link information such as a coupon code, you may
        // want to wait to call `convertInvitation` until the time when the user actually
        // uses the deep link data, rather than immediately upon receipt
        AppInvite.AppInviteApi.convertInvitation(getGoogleApiClient(), invitationId);
    }
    // [END update_invitation_status]

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        // We got a referral invitation ID before the GoogleApiClient was connected,
        // so send it now
        if (mCachedInvitationIntent != null) {
            updateInvitationStatus(mCachedInvitationIntent);
            mCachedInvitationIntent = null;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        super.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        super.onConnectionFailed(connectionResult);
        Timber.d("googleApiClient:onConnectionFailed:" + connectionResult.getErrorCode());
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Timber.w("onConnectionFailed because an API was unavailable");
        }
    }
}
