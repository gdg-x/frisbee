/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.api;

import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import com.android.volley.toolbox.*;
import com.jakewharton.DiskLruCache;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.gdg.frisbee.android.app.App;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An HttpStack that performs request over an {@link HttpClient}.
 */
public class GdgStack implements HttpStack {

    private static final String LOG_TAG = "GDG-GdgStack";
    private static final String BASE_URL = "https://developers.google.com";
    private static final String DIRECTORY_URL = BASE_URL + "/groups/directory/";
    private static final String USER_AGENT = "GDG-Frisbee/0.1 (Android)";
    private static String mCsrfToken = null;

    private HttpStack mInnerStack;

    public GdgStack() {
        if (Build.VERSION.SDK_INT >= 9) {
            mInnerStack = new OkStack(); // SPDY+HTTP
        } else {
            // Prior to Gingerbread, HttpUrlConnection was unreliable.
            // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
            mInnerStack = new HttpClientStack(AndroidHttpClient.newInstance(USER_AGENT));
        }
    }

    private void acquireCsrfToken() {
        StringRequest csrfRequest = new StringRequest(DIRECTORY_URL, null, null);
        HashMap<String, String> additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put(HTTP.USER_AGENT, USER_AGENT);

        try {
            HttpResponse response = performRequest(csrfRequest, additionalHeaders);

            if(response.getStatusLine().getStatusCode() == 200) {
                Header csrfCookie = response.getFirstHeader("Set-Cookie");
                if(csrfCookie != null && csrfCookie.getValue().contains("csrftoken")) {
                    Pattern pattern = Pattern.compile("csrftoken=([a-z0-9]{32})");
                    Matcher matcher = pattern.matcher(csrfCookie.getValue());
                    if(matcher.find()) {
                        mCsrfToken = matcher.group(1);
                        Log.d(LOG_TAG, "Got csrf token: " + mCsrfToken);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {

        if(request.getMethod() == Request.Method.POST) {
            if(mCsrfToken == null)
                acquireCsrfToken();

            additionalHeaders.put("Cookie", "csrftoken="+ mCsrfToken);
            additionalHeaders.put("X-CSRFToken", mCsrfToken);
        }

        return mInnerStack.performRequest(request, additionalHeaders);
    }
}