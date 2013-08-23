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
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 08.07.13
 * Time: 02:08
 * To change this template use File | Settings | File Templates.
 */
public class GitHub {

    private static final String BASE_URL = " https://api.github.com";
    private static final String CONTRIBUTORS_URL = BASE_URL + "/repos/%s/%s/contributors";

    public ApiRequest getContributors(String user, String repo, Response.Listener<ArrayList<Contributor>> successListener, Response.ErrorListener errorListener) {

        Type type = new TypeToken<ArrayList<Contributor>>() {}.getType();

        String url = String.format(CONTRIBUTORS_URL, user, repo);

        GsonRequest<Void, ArrayList<Contributor>> eventReq = new GsonRequest<Void, ArrayList<Contributor>>(Request.Method.GET,
                url,
                type,
                successListener,
                errorListener,
                GsonRequest.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));

        return new ApiRequest(eventReq);
    }
}
