package org.gdg.frisbee.android.app;

import android.content.SharedPreferences;

import com.google.android.gms.common.api.GoogleApiClient;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.PrefUtils;

public class OrganizerChecker {
    private boolean mIsOrganizer = false;
    private long mLastOrganizerCheck = 0;
    private String mCheckedId = null;
    private SharedPreferences mPreferences;

    public OrganizerChecker(SharedPreferences preferences) {
        mPreferences = preferences;
        initOrganizer();
    }

    public void initOrganizer() {
        mLastOrganizerCheck = mPreferences.getLong(Const.PREF_ORGANIZER_CHECK_TIME, 0);
        mCheckedId = mPreferences.getString(Const.PREF_ORGANIZER_CHECK_ID, null);
        mIsOrganizer = mPreferences.getBoolean(Const.PREF_ORGANIZER_STATE, false);
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

    public void checkOrganizer(GoogleApiClient apiClient, final Callbacks responseHandler) {
        if (!PrefUtils.isSignedIn(apiClient.getContext())) {
            mIsOrganizer = false;
            mCheckedId = null;
            responseHandler.onOrganizerResponse(mIsOrganizer);
            return;
        }

        final String currentId = PlusUtils.getCurrentPersonId(apiClient);

        if (currentId != null
            && (!currentId.equals(mCheckedId)
            || System.currentTimeMillis() > mLastOrganizerCheck + Const.ORGANIZER_CHECK_MAX_TIME)) {
            mIsOrganizer = false;
            App.getInstance().getGdgXHub().checkOrganizer(currentId).enqueue(new Callback<OrganizerCheckResponse>() {
                @Override
                public void success(OrganizerCheckResponse organizerCheckResponse) {
                    mLastOrganizerCheck = System.currentTimeMillis();
                    mCheckedId = currentId;
                    mIsOrganizer = organizerCheckResponse.getChapters().size() > 0;
                    responseHandler.onOrganizerResponse(mIsOrganizer);

                    savePreferences();
                }

                @Override
                public void failure(Throwable error) {
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
