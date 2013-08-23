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
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.GcmRegistrationRequest;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.api.model.MessageResponse;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 21.08.13
 * Time: 23:41
 * To change this template use File | Settings | File Templates.
 */
public class GdgX {

    private static final String BASE_URL = "https://gdg-x.hp.af.cm";
    private static final String GCM_REGISTER_URL = BASE_URL + "/api/v1/gcm/register";

    private static final String LOG_TAG = "GDG-GDGX";

    public GdgX() {
    }

    public ApiRequest registerGcm(String token, String regId, Response.Listener<GcmRegistrationResponse> successListener, Response.ErrorListener errorListener) {
        GcmRegistrationRequest request =  new GcmRegistrationRequest();
        request.setRegistrationId(regId);

        GsonRequest<GcmRegistrationRequest, GcmRegistrationResponse> gcmReq = new GsonRequest<GcmRegistrationRequest, GcmRegistrationResponse>(Request.Method.POST,
                GCM_REGISTER_URL,
                request,
                GcmRegistrationResponse.class,
                successListener,
                errorListener);
        gcmReq.setToken(token);

        return new ApiRequest(gcmReq);
    }
}
