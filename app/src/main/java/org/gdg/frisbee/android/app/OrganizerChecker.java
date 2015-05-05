package org.gdg.frisbee.android.app;

import android.content.SharedPreferences;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.utils.PrefUtils;

import retrofit.Callback;
import retrofit.RetrofitError;

public class OrganizerChecker {
    private boolean mIsOrganizer = false;
    private long mLastOrganizerCheck = 0;
    private String mCheckedId = null;
    private SharedPreferences mPreferences;

    public OrganizerChecker(SharedPreferences preferences) {
        mPreferences = preferences;

        mLastOrganizerCheck = mPreferences.getLong(Const.PREF_ORGANIZER_CHECK_TIME, 0);
        mCheckedId = mPreferences.getString(Const.PREF_ORGANIZER_CHECK_ID, null);
    }

    public long getLastOrganizerCheckTime() {
        return mLastOrganizerCheck;
    }

    public String getLastOrganizerCheckId() {
        return mCheckedId;
    }

    public boolean isOrganizer() {
        return mIsOrganizer;
    }

    public void checkOrganizer(GoogleApiClient apiClient, final OrganizerResponseHandler responseHandler) {
        final Person plusPerson = PrefUtils.isSignedIn(apiClient.getContext())
                ? Plus.PeopleApi.getCurrentPerson(apiClient) : null;
        final String currentId = plusPerson != null ? plusPerson.getId() : null;

        if (currentId == null 
                || !currentId.equals(mCheckedId)  
                || System.currentTimeMillis() > mLastOrganizerCheck + Const.ORGANIZER_CHECK_MAX_TIME) {
            mIsOrganizer = false;
            App.getInstance().getGdgXHub().checkOrganizer(currentId, new Callback<OrganizerCheckResponse>() {
                @Override
                public void success(OrganizerCheckResponse organizerCheckResponse, retrofit.client.Response response) {
                    mLastOrganizerCheck = System.currentTimeMillis();
                    mCheckedId = currentId;
                    savePreferences();

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

    private void savePreferences() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(Const.PREF_ORGANIZER_CHECK_TIME, getLastOrganizerCheckTime());
        editor.putString(Const.PREF_ORGANIZER_CHECK_ID, getLastOrganizerCheckId());
        editor.apply();
    }

    public interface OrganizerResponseHandler {
        void onOrganizerResponse(boolean isOrganizer);

        void onErrorResponse();
    }
}
