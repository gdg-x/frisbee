package org.gdg.frisbee.android.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

import org.gdg.frisbee.android.app.GoogleApiClientFactory;

public class PlusUtils {

    @Nullable
    public static String getCurrentPersonId(Context context) {
        GoogleSignInAccount account = getCurrentAccount(context);
        return account != null ? account.getId() : null;
    }

    @Nullable
    public static GoogleSignInAccount getCurrentAccount(Context context) {
        GoogleApiClient client = GoogleApiClientFactory.createForSignIn(context);

        OptionalPendingResult<GoogleSignInResult> result = Auth.GoogleSignInApi.silentSignIn(client);
        if (!result.isDone()) {
            return null;
        }
        return result.get().getSignInAccount();
    }

    private PlusUtils() {
        //no instance
    }
}
