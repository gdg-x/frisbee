package org.gdg.frisbee.android.onboarding;

import android.support.v4.app.ShareCompat;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PlusUtils;

import okhttp3.HttpUrl;

public class AppInviteLinkGenerator {

    private static final String SENDER = "sender";
    private static final String SUPPORTED_VERSION_CODE = "26000";
    private static final HttpUrl NON_SIGNED_IN_INVITE_URL = HttpUrl.parse("https://fmec6.app.goo.gl/Lr7u");

    private final String dynamicLinkDomain;
    private final String deepLinkBaseUrl;

    static String extractSender(HttpUrl httpUrl) {
        return httpUrl.queryParameter(SENDER);
    }

    public static AppInviteLinkGenerator create() {
        return new AppInviteLinkGenerator("https://fmec6.app.goo.gl/", "https://gdg.events/");
    }

    private AppInviteLinkGenerator(String dynamicLinkDomain, String deepLinkBaseUrl) {
        this.dynamicLinkDomain = dynamicLinkDomain;
        this.deepLinkBaseUrl = deepLinkBaseUrl;
    }

    public static void shareAppInviteLink(GdgActivity activity) {
        AppInviteLinkGenerator linkGenerator = create();
        String gplusId = PlusUtils.getCurrentPersonId(activity.getGoogleApiClient());
        HttpUrl appInviteLink = gplusId != null
            ? linkGenerator.createAppInviteLink(gplusId)
            : NON_SIGNED_IN_INVITE_URL;
        ShareCompat.IntentBuilder.from(activity)
            .setChooserTitle(R.string.invite_friends)
            .setText(activity.getString(R.string.invitation_message, appInviteLink))
            .setType("text/plain")
            .startChooser();

        activity.sendAnalyticsEvent("AppInvite", "Shared",
            gplusId != null ? "Signed In" : "Non Signed In");
    }

    private HttpUrl createAppInviteLink(String gplusId) {
        return HttpUrl.parse(dynamicLinkDomain)
            .newBuilder()
            .addQueryParameter("link", createDeepLink(gplusId))
            .addQueryParameter("apn", BuildConfig.APPLICATION_ID)
            .addQueryParameter("amv", SUPPORTED_VERSION_CODE)
            .build();
    }

    private String createDeepLink(String gplusId) {
        return HttpUrl.parse(deepLinkBaseUrl)
            .newBuilder()
            .addQueryParameter(SENDER, gplusId)
            .build()
            .toString();
    }

}
