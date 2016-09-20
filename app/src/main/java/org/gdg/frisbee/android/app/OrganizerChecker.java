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

    private final SharedPreferences preferences;

    private boolean isOrganizer = false;
    private long lastOrganizerCheckTimeStamp = 0;
    private String checkedId = null;

    OrganizerChecker(SharedPreferences preferences) {
        this.preferences = preferences;
        initOrganizer();
    }

    private void initOrganizer() {
        lastOrganizerCheckTimeStamp = preferences.getLong(PREF_ORGANIZER_CHECK_TIME, 0);
        checkedId = preferences.getString(PREF_ORGANIZER_CHECK_ID, null);
        isOrganizer = preferences.getBoolean(PREF_ORGANIZER_STATE, false);
    }

    void resetOrganizer() {
        preferences.edit()
            .putBoolean(OrganizerChecker.PREF_ORGANIZER_STATE, false)
            .putLong(OrganizerChecker.PREF_ORGANIZER_CHECK_TIME, 0)
            .apply();
    }

    public boolean isOrganizer() {
        return isOrganizer;
    }

    void checkOrganizer(GoogleApiClient apiClient, final Callbacks responseHandler) {
        if (!PrefUtils.isSignedIn(apiClient.getContext())) {
            isOrganizer = false;
            checkedId = null;
            responseHandler.onOrganizerResponse(false);
            return;
        }

        final String currentId = PlusUtils.getCurrentPersonId(apiClient);

        if (currentId != null
            && (!currentId.equals(checkedId)
            || System.currentTimeMillis() > lastOrganizerCheckTimeStamp + ORGANIZER_CHECK_MAX_TIME)) {
            isOrganizer = false;
            App.getInstance().getGdgXHub().checkOrganizer(currentId).enqueue(new Callback<OrganizerCheckResponse>() {
                @Override
                public void success(OrganizerCheckResponse organizerCheckResponse) {
                    lastOrganizerCheckTimeStamp = System.currentTimeMillis();
                    checkedId = currentId;
                    isOrganizer = organizerCheckResponse.getChapters().size() > 0;
                    responseHandler.onOrganizerResponse(isOrganizer);

                    savePreferences();
                }

                @Override
                public void failure(Throwable error) {
                    isOrganizer = false;
                    responseHandler.onErrorResponse();
                }
            });
        } else {
            responseHandler.onOrganizerResponse(isOrganizer);
        }
    }

    private void savePreferences() {
        preferences.edit()
            .putLong(PREF_ORGANIZER_CHECK_TIME, lastOrganizerCheckTimeStamp)
            .putString(PREF_ORGANIZER_CHECK_ID, checkedId)
            .putBoolean(PREF_ORGANIZER_STATE, isOrganizer())
            .apply();
    }

    public interface Callbacks {
        void onOrganizerResponse(boolean isOrganizer);

        void onErrorResponse();
    }
}
