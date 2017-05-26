package org.gdg.frisbee.android.onboarding;

import android.app.ProgressDialog;
import android.support.v4.app.ShareCompat;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.FirebaseDynamicLinksHub;
import org.gdg.frisbee.android.api.model.FirebaseDynamicLinksResponse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PlusUtils;

import okhttp3.HttpUrl;

public class AppInviteLinkGenerator {

    private static final String SENDER = "sender";
    private static final HttpUrl NON_SIGNED_IN_INVITE_URL = HttpUrl.parse("https://fmec6.app.goo.gl/bVbA");

    private final String dynamicLinkDomain;
    private final String deepLinkBaseUrl;
    private GdgActivity activity;
    private ProgressDialog shareProgressDialog;

    static String extractSender(HttpUrl httpUrl) {
        return httpUrl.queryParameter(SENDER);
    }

    public static AppInviteLinkGenerator create(GdgActivity activity) {
        return new AppInviteLinkGenerator("https://fmec6.app.goo.gl/", "https://gdg.events/", activity);
    }

    private AppInviteLinkGenerator(String dynamicLinkDomain, String deepLinkBaseUrl, GdgActivity activity) {
        this.dynamicLinkDomain = dynamicLinkDomain;
        this.deepLinkBaseUrl = deepLinkBaseUrl;
        this.activity = activity;
    }

    public static void shareAppInviteLink(GdgActivity activity) {
        AppInviteLinkGenerator linkGenerator = create(activity);
        String gplusId = PlusUtils.getCurrentPlusId(activity);
        HttpUrl appInviteLink;
        if (gplusId != null) {
            appInviteLink = linkGenerator.createAppInviteLink(gplusId);
            linkGenerator.shareShortAppInviteLink(appInviteLink.toString());
            linkGenerator.shareProgressDialog = new ProgressDialog(activity);
            linkGenerator.shareProgressDialog.setIndeterminate(true);
            linkGenerator.shareProgressDialog.setCancelable(true);
            linkGenerator.shareProgressDialog.setMessage(activity.getString(R.string.generating_url));
            linkGenerator.shareProgressDialog.show();
        } else {
            appInviteLink = NON_SIGNED_IN_INVITE_URL;
            linkGenerator.createChooser(appInviteLink);
        }


        activity.sendAnalyticsEvent("AppInvite", "Shared",
            gplusId != null ? "Signed In" : "Non Signed In");
    }

    private HttpUrl createAppInviteLink(String gplusId) {
        return HttpUrl.parse(dynamicLinkDomain)
            .newBuilder()
            .addQueryParameter("link", createDeepLink(gplusId))
            .addQueryParameter("apn", BuildConfig.APPLICATION_ID)
            .build();
    }

    private String createDeepLink(String gplusId) {
        return HttpUrl.parse(deepLinkBaseUrl)
            .newBuilder()
            .addPathSegment("invite")
            .addQueryParameter(SENDER, gplusId)
            .build()
            .toString();
    }

    private void shareShortAppInviteLink(String longUrl) {
        FirebaseDynamicLinksHub firebaseDynamicLinksHub = App.from(activity).getFirebaseDynamicLinksHub();
        firebaseDynamicLinksHub.getShortenedUrl(BuildConfig.FIREBASE_WEB_API_KEY, longUrl).enqueue(new Callback<FirebaseDynamicLinksResponse>() {
            @Override
            public void onSuccess(FirebaseDynamicLinksResponse response) {
                createChooser(HttpUrl.parse(response.getShortLink()));
                shareProgressDialog.dismiss();
            }

            @Override
            public void onError() {
                createChooser(NON_SIGNED_IN_INVITE_URL);
                shareProgressDialog.dismiss();
            }
        });
    }

    private void createChooser(HttpUrl appInviteLink) {
        ShareCompat.IntentBuilder.from(activity)
            .setChooserTitle(R.string.invite_friends)
            .setText(activity.getString(R.string.invitation_message, appInviteLink))
            .setType("text/plain")
            .startChooser();
    }
}
