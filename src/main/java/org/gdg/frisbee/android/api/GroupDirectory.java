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

import android.content.Context;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.gdg.frisbee.android.api.deserializer.DateTimeDeserializer;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.Response;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api
 * <p/>
 * User: maui
 * Date: 21.04.13
 * Time: 22:08
 */
public class GroupDirectory {

    private static final String BASE_URL = "https://developers.google.com";
    private static final String DIRECTORY_URL = BASE_URL + "/groups/directory/";
    private static final String ALL_CALENDAR_URL = BASE_URL + "/events/calendar/fc?start=1366581600&end=1367186400&_=1366664352089";
    private static final String GDL_CALENDAR_URL = BASE_URL + "/events/calendar/fc?calendar=gdl&start=1366581600&end=1367186400&_=1366664644691";
    private static final String CHAPTER_CALENDAR_URL = BASE_URL + "/groups/chapter/%s/feed/events/fc";
    private static final String SHOWCASE_NEXT_URL = BASE_URL + "/showcase/next";

    private static final String LOG_TAG = "GDG-GroupDirectory";
    private static final String USER_AGENT = "GDG-Android/0.1";

    private HttpClient mHttpClient;
    private Gson mGson;
    private static String mCsrfToken = null;

    private Context mContext;
    private static RequestQueue mRequestQueue;

    public GroupDirectory(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        mContext = context;
        mHttpClient = new DefaultHttpClient();
    }

    public Directory getDirectory() throws ApiException {
        if(mCsrfToken == null)
            mCsrfToken = getCsrfToken();
        Directory dir = (Directory) postRaw(DIRECTORY_URL, "", Directory.class, FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        return dir;
    }

    public ArrayList<Event> getChapterEventList(final DateTime start, final DateTime end, String chapterId) throws ApiException {
        if(mCsrfToken == null)
            mCsrfToken = getCsrfToken();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new NameValuePair() {
            @Override
            public String getName() {
                return "start";
            }

            @Override
            public String getValue() {
                return ""+(int)(start.getMillis()/1000);
            }
        });
        params.add(new NameValuePair() {
            @Override
            public String getName() {
                return "end";
            }

            @Override
            public String getValue() {
                return ""+(int)(end.getMillis()/1000);
            }
        });
        params.add(new NameValuePair() {
            @Override
            public String getName() {
                return "_";
            }

            @Override
            public String getValue() {
                return ""+(int)(new DateTime().getMillis()/1000);
            }
        });
        Type type = new TypeToken<ArrayList<Event>>() {}.getType();
        return (ArrayList<Event>) getRaw(String.format(CHAPTER_CALENDAR_URL,chapterId), params, type, FieldNamingPolicy.IDENTITY);
    }

    private String getCsrfToken() throws ApiException {
        HttpGet request = new HttpGet(DIRECTORY_URL);
        request.setHeader(new BasicHeader(HTTP.USER_AGENT,USER_AGENT));

        try {
            HttpResponse response = mHttpClient.execute(request);
            if(response.getStatusLine().getStatusCode() == 200) {
                Header csrfCookie = response.getFirstHeader("Set-Cookie");
                if(csrfCookie != null && csrfCookie.getValue().contains("csrftoken")) {
                    Pattern pattern = Pattern.compile("csrftoken=([a-z0-9]{32})");
                    Matcher matcher = pattern.matcher(csrfCookie.getValue());
                    if(matcher.find()) {
                        return matcher.group(1);
                    }
                }
                throw new ApiException("Could not acquire CSRF Token");
            } else {
                throw new ApiException(response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException(e);
        } catch (ClientProtocolException e) {
            throw new ApiException(e);
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (IllegalStateException e) {
            throw new ApiException(e);
        }
    }

    private Object getRaw(String url, List<NameValuePair> params, Type type, FieldNamingPolicy policy) throws ApiException {

        if(params != null) {
            url += "?"+URLEncodedUtils.format(params, "UTF-8");
        }

        HttpGet request = new HttpGet(url);
        request.setHeader(new BasicHeader(HTTP.USER_AGENT,USER_AGENT));
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json; charset=utf-8"));
        request.setHeader(new BasicHeader("Referer", "https://developers.google.com/groups/directory"));
        request.setHeader(new BasicHeader("X-CSRFToken", mCsrfToken));

        try {
            HttpResponse response = mHttpClient.execute(request);
            String inp = Utils.inputStreamToString(response.getEntity().getContent());

            if(response.getStatusLine().getStatusCode() == 200) {
                return getGson(policy).fromJson(inp, type);
            } else {
                throw new ApiException(response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException(e);
        } catch (ClientProtocolException e) {
            throw new ApiException(e);
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (IllegalStateException e) {
            throw new ApiException(e);
        }
    }

    private Response postRaw(String url, String req, Type type, FieldNamingPolicy policy) throws ApiException {
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.USER_AGENT,USER_AGENT));
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json; charset=utf-8"));
        request.setHeader(new BasicHeader("Referer", "https://developers.google.com/groups/directory"));
        request.setHeader(new BasicHeader("X-CSRFToken", mCsrfToken));

        StringEntity s;
        try {

            if(req != null) {
                s = new StringEntity(req);
                s.setContentType("application/json; charset=utf-8");
                s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json; charset=utf-8"));
                request.setEntity(s);
            }

            HttpResponse response = mHttpClient.execute(request);
            String inp = Utils.inputStreamToString(response.getEntity().getContent());

            if(response.getStatusLine().getStatusCode() == 200) {
                return getGson(policy).fromJson(inp, type);
            } else {
                throw new ApiException(response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode());
            }
        } catch (UnsupportedEncodingException e) {
            throw new ApiException(e);
        } catch (ClientProtocolException e) {
            throw new ApiException(e);
        } catch (IOException e) {
            throw new ApiException(e);
        } catch (IllegalStateException e) {
            throw new ApiException(e);
        }
    }

    private Gson getGson(FieldNamingPolicy policy) {
        mGson = new GsonBuilder()
                    .setFieldNamingPolicy(policy)
                    .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                    .create();
        return mGson;
    }
}
