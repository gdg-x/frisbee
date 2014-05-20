/*
 * Copyright 2013 The GDG Frisbee Project
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

import android.util.Log;
import com.android.volley.*;
import timber.log.Timber;

import java.util.HashMap;
import java.util.Map;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api
 * <p/>
 * User: maui
 * Date: 26.05.13
 * Time: 19:39
 */
public abstract class GdgRequest<T> extends Request<T> {

    private static final String LOG_TAG = "GDG-GdgRequest";
    private static final String BASE_URL = "https://developers.google.com";
    private static final String DIRECTORY_URL = BASE_URL + "/groups/directory/";
    private static final String USER_AGENT = "GDG-Frisbee/0.1 (Android)";
    public static String mCsrfToken = null;

    protected Response.Listener<T> mListener;

    public GdgRequest(int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    public void setResponseListener(Response.Listener<T> listener) {
        mListener = listener;
    }

    public Response.Listener<T> getResponseListener() {
        return mListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {

        super.getHeaders();
        Timber.d("getHeaders()");

        HashMap<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put("User-Agent", USER_AGENT);
        additionalHeaders.put("Referer", "https://developers.google.com/groups/directory/");
        additionalHeaders.put("X-Requested-With", "XMLHttpRequest");
        additionalHeaders.put("Cache-Control", "no-cache");
        additionalHeaders.put("DNT", "1");
        return additionalHeaders;
    }


    @Override
    public byte[] getBody() throws AuthFailureError {
        return "".getBytes();
    }

    @Override
    public String getBodyContentType() {
        return "application/json";
    }
}
