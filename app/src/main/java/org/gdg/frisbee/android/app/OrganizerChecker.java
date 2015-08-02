package org.gdg.frisbee.android.app;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
    @Nullable
    private String mCheckedId = null;
    private SharedPreferences mPreferences;

    public OrganizerChecker(SharedPreferences preferences) {
        mPreferences = preferences;
        resetOrganizer();
    }

    public void resetOrganizer() {
        mLastOrganizerCheck = mPreferences.getLong(Const.PREF_ORGANIZER_CHECK_TIME, 0);
        mCheckedId = mPreferences.getString(Const.PREF_ORGANIZER_CHECK_ID, null);
        mIsOrganizer = mPreferences.getBoolean(Const.PREF_ORGANIZER_STATE, false);
    }

    public long getLastOrganizerCheckTime() {
        return mLastOrganizerCheck;
    }

    @Nullable
    public String getLastOrganizerCheckId() {
        return mCheckedId;
    }

    public boolean isOrganizer() {
        return mIsOrganizer;
    }

    public void checkOrganizer(@NonNull GoogleApiClient apiClient, @NonNull final Callbacks responseHandler) {
        Person plusPerson = null;
        if (apiClient.isConnected() && PrefUtils.isSignedIn(apiClient.getContext())) {
            plusPerson = Plus.PeopleApi.getCurrentPerson(apiClient);
        }
        final String currentId = plusPerson != null ? plusPerson.getId() : null;

        if (currentId != null
                && (!currentId.equals(mCheckedId)
                || System.currentTimeMillis() > mLastOrganizerCheck + Const.ORGANIZER_CHECK_MAX_TIME)) {
            mIsOrganizer = false;
            App.getInstance().getGdgXHub().checkOrganizer(currentId, new Callback<OrganizerCheckResponse>() {
                @Override
                public void success(@NonNull OrganizerCheckResponse organizerCheckResponse, retrofit.client.Response response) {
                    mLastOrganizerCheck = System.currentTimeMillis();
                    mCheckedId = currentId;
                    mIsOrganizer = organizerCheckResponse.getChapters().size() > 0;
                    responseHandler.onOrganizerResponse(mIsOrganizer);

                    savePreferences();
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
        mPreferences.edit()
                .putLong(Const.PREF_ORGANIZER_CHECK_TIME, getLastOrganizerCheckTime())
                .putString(Const.PREF_ORGANIZER_CHECK_ID, getLastOrganizerCheckId())
                .putBoolean(Const.PREF_ORGANIZER_STATE, isOrganizer())
                .apply();
    }

    public interface Callbacks {
        void onOrganizerResponse(boolean isOrganizer);

        void onErrorResponse();
    }
}
