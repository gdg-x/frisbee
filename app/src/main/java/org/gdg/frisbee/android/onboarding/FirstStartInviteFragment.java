package org.gdg.frisbee.android.onboarding;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.invite.AppInviteLinkGenerator;

import okhttp3.HttpUrl;

public class FirstStartInviteFragment extends Fragment {

    private Listener listener = Listener.EMPTY;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Listener) {
            listener = (Listener) context;
        }

        if (context instanceof GdgActivity) {
            GoogleApiClient client = ((GdgActivity) context).getGoogleApiClient();
            AppInvite.AppInviteApi.getInvitation(client, getActivity(), false)
                .setResultCallback(new ResultCallback<AppInviteInvitationResult>() {
                    @Override
                    public void onResult(@NonNull AppInviteInvitationResult result) {
                        onInvitationLoaded(result);
                    }
                });
        }
    }

    private void onInvitationLoaded(@NonNull AppInviteInvitationResult result) {
        if (result.getStatus().isSuccess()) {
            Intent intent = result.getInvitationIntent();
            String sender = extractSender(intent);

            if (sender != null) {
                updateSender(sender);
            } else {
                listener.onInviteSkipped();
            }
        }
    }

    private void updateSender(String sender) {
        // TODO: 9/20/16 update UI
    }

    private String extractSender(Intent intent) {
        String deepLink = AppInviteReferral.getDeepLink(intent);
        if (deepLink == null) {
            return null;
        }

        HttpUrl httpUrl = HttpUrl.parse(deepLink);
        if (httpUrl == null) {
            return null;
        }
        return httpUrl.queryParameter(AppInviteLinkGenerator.SENDER);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = Listener.EMPTY;
    }

    public interface Listener {
        void onInviteSkipped();

        Listener EMPTY = new Listener() {
            @Override
            public void onInviteSkipped() {

            }
        };
    }

}
