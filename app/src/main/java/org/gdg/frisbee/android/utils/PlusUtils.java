package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.Scope;

public class PlusUtils {

    @Nullable
    public static String getCurrentPersonId(Context context) {
        GoogleSignInAccount account = getCurrentAccount(context);
        return account != null ? account.getId() : null;
    }

    @Nullable
    public static GoogleSignInAccount getCurrentAccount(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(new Scope(Scopes.PLUS_LOGIN))
            .build();

        OptionalPendingResult<GoogleSignInResult> result = Auth.GoogleSignInApi
            .silentSignIn(new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()
            );
        if (!result.isDone()) {
            return null;
        }
        return result.get().getSignInAccount();
    }

    private PlusUtils() {
        //no instance
    }
}
