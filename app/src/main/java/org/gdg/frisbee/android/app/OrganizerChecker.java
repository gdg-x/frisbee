package org.gdg.frisbee.android.app;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.api.GdgX;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;

public class OrganizerChecker {
    private final Context mContext;
    private boolean isOrganizer = false;
    private long lastOrganizerCheck = 0;

    public OrganizerChecker(Context context) {
        mContext = context;
    }

    public void setLastOrganizerCheck(long lastOrganizerCheck) {
        this.lastOrganizerCheck = lastOrganizerCheck;
    }

    public boolean isOrganizer() {
        return isOrganizer;
    }

    public void checkOrganizer(GoogleApiClient apiClient, final OrganizerResponseHandler responseHandler) {
        if (System.currentTimeMillis() > lastOrganizerCheck + 24 * 60 * 60 * 1000) {
            GdgX xClient = new GdgX();
            xClient.checkOrganizer(Plus.PeopleApi.getCurrentPerson(apiClient).getId(), new Response.Listener<OrganizerCheckResponse>() {
                @Override
                public void onResponse(OrganizerCheckResponse organizerCheckResponse) {
                    lastOrganizerCheck = System.currentTimeMillis();

                    if (organizerCheckResponse.getChapters().size() > 0) {
                        isOrganizer = true;
                        responseHandler.onOrganizerResponse(true);
                    } else {
                        isOrganizer = false;
                        responseHandler.onOrganizerResponse(false);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    isOrganizer = false;
                    responseHandler.onErrorResponse();
                }
            }).execute();
        } else {
            responseHandler.onOrganizerResponse(isOrganizer);
        }
    }

    public static interface OrganizerResponseHandler {
        public void onOrganizerResponse(boolean isOrganizer);

        public void onErrorResponse();
    }
}
