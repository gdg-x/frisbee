package org.gdg.frisbee.android.invite;

import org.gdg.frisbee.android.BuildConfig;

import okhttp3.HttpUrl;

public class AppInviteLinkGenerator {

    public static final String SENDER = "sender";
    private static final String SUPPORTED_VERSION_CODE = "26000";
    private final String dynamicLinkDomain;
    private final String deepLinkBaseUrl;

    public AppInviteLinkGenerator(String dynamicLinkDomain, String deepLinkBaseUrl) {
        this.dynamicLinkDomain = dynamicLinkDomain;
        this.deepLinkBaseUrl = deepLinkBaseUrl;
    }

    public HttpUrl createAppInviteLink(String plusUrl) {
        return HttpUrl.parse(dynamicLinkDomain)
            .newBuilder()
            .addQueryParameter("link", createDeepLink(plusUrl))
            .addQueryParameter("apn", BuildConfig.APPLICATION_ID)
            .addQueryParameter("amv", SUPPORTED_VERSION_CODE)
            .build();
    }

    private String createDeepLink(String plusUrl) {
        return HttpUrl.parse(deepLinkBaseUrl)
            .newBuilder()
            .addPathSegment("invite")
            .addQueryParameter(SENDER, plusUrl)
            .build()
            .toString();
    }

}
