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
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
            .addApi(AppIndex.API);

        if (PrefUtils.isSignedIn(context)) {
            builder.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN).addScope(Plus.SCOPE_PLUS_PROFILE)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER);
        }

        return builder.build();
    }

    public static GoogleApiClient createWithoutSignIn(Context context) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context)
            .addApi(AppIndex.API);
        return builder.build();
    }
}
