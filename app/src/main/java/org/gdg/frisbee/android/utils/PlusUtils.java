package org.gdg.frisbee.android.utils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class PlusUtils {

    private PlusUtils() {
    }

    @Nullable
    public static String getCurrentPersonId(GoogleApiClient apiClient) {
        Person plusPerson = null;
        if (apiClient.isConnected() && apiClient.hasConnectedApi(Plus.API)) {
            plusPerson = Plus.PeopleApi.getCurrentPerson(apiClient);
        }
        return plusPerson != null ? plusPerson.getId() : null;
    }

    public static Uri createProfileUrl(@NonNull final String gplusId) {
        return Uri.parse("https://plus.google.com/" + gplusId);
    }
}
