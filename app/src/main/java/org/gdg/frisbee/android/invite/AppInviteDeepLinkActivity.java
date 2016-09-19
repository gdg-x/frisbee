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

package org.gdg.frisbee.android.invite;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.Toast;

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

    @Override
    protected GoogleApiClient createGoogleApiClient() {
        return GoogleApiClientFactory.createWithoutSignIn(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new AppInviteDialogFragment().show(
            getSupportFragmentManager(),
            AppInviteDeepLinkActivity.class.getSimpleName()
        );
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        processReferralIntent(intent);
    }

    private void processReferralIntent(Intent intent) {
        if (!AppInviteReferral.hasReferral(intent)) {
            Timber.e("Error: AppInviteDeepLinkActivity Intent does not contain App Invite");
            return;
        }

        // Extract referral information from the intent
        String invitationId = AppInviteReferral.getInvitationId(intent);
        String deepLink = AppInviteReferral.getDeepLink(intent);

        Toast.makeText(this, deepLink, Toast.LENGTH_SHORT).show();
        // TODO: 9/19/16 handle deeplink
    }

    @Override
    public void onConnectionSuspended(int i) {
        super.onConnectionSuspended(i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        super.onConnectionFailed(connectionResult);
        Timber.d("googleApiClient:onConnectionFailed: %s", connectionResult.getErrorCode());
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Timber.w("onConnectionFailed because an API was unavailable");
        }
    }

    public static class AppInviteDialogFragment extends AppCompatDialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                .setTitle("Congrats!")
                .setMessage("You installed the app with an invite. Here is your reward!")
                .create();
        }
    }
}
