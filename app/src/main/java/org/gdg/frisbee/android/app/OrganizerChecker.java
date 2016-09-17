package org.gdg.frisbee.android.app;

import android.content.SharedPreferences;
import android.text.format.DateUtils;

import com.google.android.gms.common.api.GoogleApiClient;

import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.PrefUtils;

public class OrganizerChecker {

    private static final String PREF_ORGANIZER_CHECK_TIME = "pref_organizer_check_time";
    private static final String PREF_ORGANIZER_CHECK_ID = "pref_organizer_check_id";
    private static final String PREF_ORGANIZER_STATE = "pref_organizer_state";
    private static final long ORGANIZER_CHECK_MAX_TIME = DateUtils.WEEK_IN_MILLIS;

    private boolean mIsOrganizer = false;
    private long mLastOrganizerCheck = 0;
    private String mCheckedId = null;
    private SharedPreferences mPreferences;

    OrganizerChecker(SharedPreferences preferences) {
        mPreferences = preferences;
        initOrganizer();
    }

    void resetOrganizer() {
        mPreferences.edit()
            .putBoolean(OrganizerChecker.PREF_ORGANIZER_STATE, false)
            .putLong(OrganizerChecker.PREF_ORGANIZER_CHECK_TIME, 0)
            .apply();
    }

    private void initOrganizer() {
        mLastOrganizerCheck = mPreferences.getLong(PREF_ORGANIZER_CHECK_TIME, 0);
        mCheckedId = mPreferences.getString(PREF_ORGANIZER_CHECK_ID, null);
        mIsOrganizer = mPreferences.getBoolean(PREF_ORGANIZER_STATE, false);
    }

    public boolean isOrganizer() {
        return mIsOrganizer;
    }

    void checkOrganizer(GoogleApiClient apiClient, final Callbacks responseHandler) {
        if (!PrefUtils.isSignedIn(apiClient.getContext())) {
            mIsOrganizer = false;
            mCheckedId = null;
            responseHandler.onOrganizerResponse(false);
            return;
        }

        final String currentId = PlusUtils.getCurrentPersonId(apiClient);

        if (currentId != null
            && (!currentId.equals(mCheckedId)
            || System.currentTimeMillis() > mLastOrganizerCheck + ORGANIZER_CHECK_MAX_TIME)) {
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
            .putLong(PREF_ORGANIZER_CHECK_TIME, mLastOrganizerCheck)
            .putString(PREF_ORGANIZER_CHECK_ID, mCheckedId)
            .putBoolean(PREF_ORGANIZER_STATE, isOrganizer())
            .apply();
    }

    public interface Callbacks {
        void onOrganizerResponse(boolean isOrganizer);

        void onErrorResponse();
    }
}
