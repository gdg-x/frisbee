package org.gdg.frisbee.android.app;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.utils.PrefUtils;

import retrofit.Callback;
import retrofit.RetrofitError;

public class OrganizerChecker {
    private boolean mIsOrganizer = false;

    public OrganizerChecker() {
    }

    public boolean isOrganizer() {
        return mIsOrganizer;
    }

    public void checkOrganizer(GoogleApiClient apiClient, final Callbacks responseHandler) {
        Person plusPerson = null;
        if (apiClient.isConnected() && PrefUtils.isSignedIn(apiClient.getContext())) {
            plusPerson = Plus.PeopleApi.getCurrentPerson(apiClient);
        }
        final String currentId = plusPerson != null ? plusPerson.getId() : null;

        if (currentId != null) {
            mIsOrganizer = false;
            App.getInstance().getGdgXHub().checkOrganizer(currentId, new Callback<OrganizerCheckResponse>() {
                @Override
                public void success(OrganizerCheckResponse organizerCheckResponse, retrofit.client.Response response) {

                    if (organizerCheckResponse.getChapters().size() > 0) {
                        mIsOrganizer = true;
                        responseHandler.onOrganizerResponse(true);
                    } else {
                        mIsOrganizer = false;
                        responseHandler.onOrganizerResponse(false);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    mIsOrganizer = false;
                    responseHandler.onErrorResponse();
                }
            });
        } else {
            responseHandler.onOrganizerResponse(mIsOrganizer);
        }
    }

    public interface Callbacks {
        void onOrganizerResponse(boolean isOrganizer);
        void onErrorResponse();
    }
}
