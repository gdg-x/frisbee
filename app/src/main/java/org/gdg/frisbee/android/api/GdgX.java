/*
 * Copyright 2013 GDG[x]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.api;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.FieldNamingPolicy;
import org.gdg.frisbee.android.api.model.*;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 21.08.13
 * Time: 23:41
 * To change this template use File | Settings | File Templates.
 */
public class GdgX {

    private static final String BASE_URL = "https://hub.gdgx.io";
    private static final String GCM_REGISTER_URL = BASE_URL + "/api/v1/frisbee/gcm/register";
    private static final String GCM_UNREGISTER_URL = BASE_URL + "/api/v1/frisbee/gcm/unregister";
    private static final String GDGX_HOME_GDG_URL = BASE_URL + "/api/v1/frisbee/user/home";

    private static final String LOG_TAG = "GDG-GDGX";
    private String mToken;

    public GdgX() {
    }

    public GdgX(String token) {
        mToken = token;
    }

    public ApiRequest registerGcm(String regId, Response.Listener<GcmRegistrationResponse> successListener, Response.ErrorListener errorListener) {
        GcmRegistrationRequest request =  new GcmRegistrationRequest();
        request.setRegistrationId(regId);

        GsonRequest<GcmRegistrationRequest, GcmRegistrationResponse> gcmReq = new GsonRequest<GcmRegistrationRequest, GcmRegistrationResponse>(Request.Method.POST,
                GCM_REGISTER_URL,
                request,
                GcmRegistrationResponse.class,
                successListener,
                errorListener);
        gcmReq.setToken(mToken);

        return new ApiRequest(gcmReq);
    }

    public ApiRequest checkOrganizer(String gplusId, Response.Listener<OrganizerCheckResponse> successListener, Response.ErrorListener errorListener) {
        GsonRequest<Void, OrganizerCheckResponse> organizerReq = new GsonRequest<Void, OrganizerCheckResponse>(Request.Method.GET,
                "http://hub.gdgx.io/api/v1/organizer/"+gplusId,
                OrganizerCheckResponse.class,
                successListener,
                errorListener,
                GsonRequest.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));
        return new ApiRequest(organizerReq);
    }

    public ApiRequest unregisterGcm(String regId, Response.Listener<GcmRegistrationResponse> successListener, Response.ErrorListener errorListener) {
        GcmRegistrationRequest request =  new GcmRegistrationRequest();
        request.setRegistrationId(regId);

        GsonRequest<GcmRegistrationRequest, GcmRegistrationResponse> gcmReq = new GsonRequest<GcmRegistrationRequest, GcmRegistrationResponse>(Request.Method.POST,
                GCM_UNREGISTER_URL,
                request,
                GcmRegistrationResponse.class,
                successListener,
                errorListener);
        gcmReq.setToken(mToken);

        return new ApiRequest(gcmReq);
    }

    public ApiRequest setHomeGdg(String homeGdg, Response.Listener<Void> successListener, Response.ErrorListener errorListener) {
        HomeGdgRequest request =  new HomeGdgRequest();
        request.setHomeGdg(homeGdg);

        GsonRequest<HomeGdgRequest, Void> gcmReq = new GsonRequest<HomeGdgRequest, Void>(Request.Method.PUT,
                GDGX_HOME_GDG_URL,
                request,
                Void.class,
                successListener,
                errorListener);
        gcmReq.setToken(mToken);

        return new ApiRequest(gcmReq);
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String mToken) {
        this.mToken = mToken;
    }
}
