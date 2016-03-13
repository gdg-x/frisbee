package org.gdg.frisbee.android.app;

import android.content.Context;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.utils.PrefUtils;

public final class GoogleApiClientFactory {
    private GoogleApiClientFactory() { }

    public static GoogleApiClient createWith(Context context) {
        return createClient(context, PrefUtils.isSignedIn(context));
    }

    public static GoogleApiClient createWithoutSignIn(Context context) {
        return createClient(context, false);
    }

    private static GoogleApiClient createClient(Context context, boolean withSignIn) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
            .addApi(AppIndex.API);

        if (withSignIn) {
            builder.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER);
        }

        return builder.build();
    }

}
