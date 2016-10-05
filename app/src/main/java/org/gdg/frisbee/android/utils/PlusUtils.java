package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
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

    private PlusUtils() {
    }

    @Nullable
    public static String getCurrentPersonId(Context context) {
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
        GoogleSignInAccount account = result.get().getSignInAccount();
        return account != null ? account.getId() : null;
    }

    public static Uri createProfileUrl(@NonNull final String gplusId) {
        return Uri.parse("https://plus.google.com/" + gplusId);
    }
}
