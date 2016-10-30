package org.gdg.frisbee.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.utils.PlusUtils;

public class OrganizerChecker {

    private static final String PREF_ORGANIZER_CHECK_TIME = "pref_organizer_check_time";
    private static final String PREF_ORGANIZER_CHECK_ID = "pref_organizer_check_id";
    private static final String PREF_ORGANIZER_STATE = "pref_organizer_state";
    private static final long ORGANIZER_CHECK_MAX_TIME = DateUtils.WEEK_IN_MILLIS;

    private final SharedPreferences preferences;
    private final GdgXHub gdgXHub;

    private boolean isOrganizer = false;
    private long lastOrganizerCheckTimeStamp = 0;
    private String checkedId = null;

    OrganizerChecker(SharedPreferences preferences, GdgXHub gdgXHub) {
        this.preferences = preferences;
        this.gdgXHub = gdgXHub;
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

    void checkOrganizer(Context context, final Callbacks responseHandler) {
        final String currentId = PlusUtils.getCurrentPlusId(context);

        if (currentId == null) {
            isOrganizer = false;
            checkedId = null;
            responseHandler.onOrganizerResponse(false);
            return;
        }

        if (isLastOrganizerCheckValid(currentId)) {
            responseHandler.onOrganizerResponse(isOrganizer);
            return;
        }

        isOrganizer = false;
        gdgXHub.checkOrganizer(currentId).enqueue(new Callback<OrganizerCheckResponse>() {
            @Override
            public void onSuccess(OrganizerCheckResponse organizerCheckResponse) {
                lastOrganizerCheckTimeStamp = System.currentTimeMillis();
                checkedId = currentId;
                isOrganizer = organizerCheckResponse.getChapters().size() > 0;
                responseHandler.onOrganizerResponse(isOrganizer);

                savePreferences();
            }

            @Override
            public void onError() {
                isOrganizer = false;
                responseHandler.onErrorResponse();
            }
        });
    }

    /**
     * The latest organizer check value is still valid
     * when we have the same organizer and we already checked within a valid timeframe.
     */
    private boolean isLastOrganizerCheckValid(String organizerIdToCheck) {
        return organizerIdToCheck.equals(checkedId)
            && System.currentTimeMillis() <= lastOrganizerCheckTimeStamp + ORGANIZER_CHECK_MAX_TIME;
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
