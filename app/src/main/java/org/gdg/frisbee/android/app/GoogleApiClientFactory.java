package org.gdg.frisbee.android.app;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;

import org.gdg.frisbee.android.R;


public final class GoogleApiClientFactory {
    private GoogleApiClientFactory() {
    }

    public static GoogleApiClient createWith(Context context) {
        return createBuilder(context)
            .build();
    }

    public static GoogleApiClient createWithApi(Context context, Api<? extends NotRequiredOptions> api) {
        return createBuilder(context)
            .addApi(api)
            .build();
    }

    private static GoogleApiClient.Builder createBuilder(Context context) {
        return new GoogleApiClient.Builder(context)
            .addApi(AppIndex.API);
    }

    public static GoogleApiClient createForSignIn(Context context) {
        return createBuilderForSignIn(context)
            .build();
    }

    public static GoogleApiClient createForSignIn(FragmentActivity fragmentActivity,
                                                  @Nullable OnConnectionFailedListener onConnectionFailedListener) {
        return createBuilderForSignIn(fragmentActivity)
            .enableAutoManage(fragmentActivity, onConnectionFailedListener)
            .build();
    }

    private static GoogleApiClient.Builder createBuilderForSignIn(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new Scope(Scopes.PLUS_LOGIN))
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .build();
        return new GoogleApiClient.Builder(context)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso);
    }

}
