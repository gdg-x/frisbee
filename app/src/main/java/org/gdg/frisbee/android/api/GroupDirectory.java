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

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.reflect.TypeToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.app.GdgVolley;
import org.joda.time.DateTime;
import java.lang.reflect.Type;
import java.util.ArrayList;

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
    private static final String DIRECTORY_URL = BASE_URL + "/groups/directorygroups/";
    private static final String ALL_CALENDAR_URL = BASE_URL + "/events/calendar/fc?start=1366581600&end=1367186400&_=1366664352089";
    private static final String GDL_CALENDAR_URL = BASE_URL + "/events/calendar/fc?calendar=gdl&start=1366581600&end=1367186400&_=1366664644691";
    private static final String CHAPTER_CALENDAR_URL = BASE_URL + "/groups/chapter/%s/feed/events/fc";
    private static final String SHOWCASE_NEXT_URL = BASE_URL + "/showcase/next";

    private static final String LOG_TAG = "GDG-GroupDirectory";

    public GroupDirectory() {
    }

    public ApiRequest getDirectory(Response.Listener<Directory> successListener, Response.ErrorListener errorListener) {
        GsonRequest<Directory> dirReq = new GsonRequest<Directory>(Request.Method.POST,
                DIRECTORY_URL,
                Directory.class,
                successListener,
                errorListener,
                GsonRequest.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));
        return new ApiRequest(dirReq);
    }

    public ApiRequest getChapterEventList(final DateTime start, final DateTime end, String chapterId, Response.Listener<ArrayList<Event>> successListener, Response.ErrorListener errorListener) {

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

        String url = String.format(CHAPTER_CALENDAR_URL,chapterId);
        url += "?"+URLEncodedUtils.format(params, "UTF-8");

        GsonRequest<ArrayList<Event>> eventReq = new GsonRequest<ArrayList<Event>>(Request.Method.GET,
                url,
                type,
                successListener,
                errorListener,
                GsonRequest.getGson(FieldNamingPolicy.IDENTITY));

        return new ApiRequest(eventReq);
    }
}
