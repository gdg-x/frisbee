package org.gdg.frisbee.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.appinvite.AppInviteReferral;

import org.gdg.frisbee.android.R;

public class ReferrerReceiver extends BroadcastReceiver {

    public ReferrerReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create deep link intent with correct action and add play store referral information
        Intent deepLinkIntent = AppInviteReferral.addPlayStoreReferrerToIntent(intent,
                new Intent(context.getString(R.string.action_deep_link)));

        // Let any listeners know about the change
        LocalBroadcastManager.getInstance(context).sendBroadcast(deepLinkIntent);
    }
}
