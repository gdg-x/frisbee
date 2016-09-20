package org.gdg.frisbee.android.app;

import android.content.Context;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.utils.PrefUtils;

public final class GoogleApiClientFactory {
    private GoogleApiClientFactory() {
    }

    public static GoogleApiClient createWith(Context context) {
        return createBuilder(context, PrefUtils.isSignedIn(context)).build();
    }

    public static GoogleApiClient createWithApi(Context context, Api<? extends NotRequiredOptions> api) {
        return createBuilder(context, PrefUtils.isSignedIn(context))
            .addApi(api)
            .build();
    }

    public static GoogleApiClient createWithoutSignIn(Context context) {
        return createBuilder(context, false).build();
    }

    private static GoogleApiClient.Builder createBuilder(Context context, boolean withSignIn) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
            .addApi(AppIndex.API);

        if (withSignIn) {
            builder.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_PROFILE);
        }

        return builder;
    }

}
