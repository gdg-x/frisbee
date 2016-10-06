package org.gdg.frisbee.android.app;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

public final class GoogleApiClientFactory {
    private GoogleApiClientFactory() {
    }

    public static GoogleApiClient createWith(Context context) {
        return new GoogleApiClient.Builder(context)
            .addApi(AppIndex.API)
            .build();
    }

    public static GoogleApiClient createForSignIn(Context context) {
        return createBuilderForSignIn(context)
            .build();
    }

    public static GoogleApiClient createForSignIn(FragmentActivity fragmentActivity,
                                                  @Nullable GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener) {
        return createBuilderForSignIn(fragmentActivity)
            .enableAutoManage(fragmentActivity, onConnectionFailedListener)
            .build();
    }

    private static GoogleApiClient.Builder createBuilderForSignIn(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new Scope(Scopes.PLUS_LOGIN))
            .build();
        return new GoogleApiClient.Builder(context)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso);
    }

}
