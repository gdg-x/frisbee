package org.gdg.frisbee.android.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.api.GdgX;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;

public class OrganizerChecker {
    private final Context mContext;
    private boolean mIsOrganizer = false;
    private long mLastOrganizerCheck = 0;
    private String mCheckedId = null;
    private SharedPreferences mPreferences;

    public OrganizerChecker(Context context, SharedPreferences preferences) {
        mContext = context;
        mPreferences = preferences;
    }

    public void setLastOrganizerCheckTime(long lastOrganizerCheck) {
        mLastOrganizerCheck = lastOrganizerCheck;
    }

    public long getLastOrganizerCheckTime(){
        return mLastOrganizerCheck;
    }

    public void setLastOrganizerCheckId(String organizerCheckId) {
        mCheckedId = organizerCheckId;
    }

    public String getLastOrganizerCheckId() {
        return mCheckedId;
    }

    public boolean isOrganizer() {
        return mIsOrganizer;
    }

    public void checkOrganizer(GoogleApiClient apiClient, final OrganizerResponseHandler responseHandler) {
        final String currentId = Plus.PeopleApi.getCurrentPerson(apiClient).getId();

        if (currentId == null || !currentId.equals(mCheckedId)  || System.currentTimeMillis() > mLastOrganizerCheck + Const.ORGANIZER_CHECK_MAX_TIME) {
            mIsOrganizer = false;
            GdgX xClient = new GdgX();
            xClient.checkOrganizer(currentId, new Response.Listener<OrganizerCheckResponse>() {
                @Override
                public void onResponse(OrganizerCheckResponse organizerCheckResponse) {
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
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    mIsOrganizer = false;
                    responseHandler.onErrorResponse();
                }
            }).execute();
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

    public static interface OrganizerResponseHandler {
        public void onOrganizerResponse(boolean isOrganizer);

        public void onErrorResponse();
    }
}
