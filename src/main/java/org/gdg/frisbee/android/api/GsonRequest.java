/**
 * Copyright 2013 Ognyan Bankov
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

import com.android.volley.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import org.apache.http.protocol.HTTP;
import org.gdg.frisbee.android.api.deserializer.DateTimeDeserializer;
import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


public class GsonRequest<T> extends GdgRequest<T> {
    private final Gson mGson;
    private final Type mClazz;


    public GsonRequest(int method,
                       String url,
                       Type clazz,
                       Listener<T> listener,
                       ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.mClazz = clazz;
        mGson = new Gson();
    }

    public GsonRequest(int method,
                       String url,
                       Type clazz,
                       Listener<T> listener,
                       ErrorListener errorListener,
                       Gson gson) {
        super(method, url, listener, errorListener);
        this.mClazz = clazz;
        mGson = gson;
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return (Response<T>)Response.success(mGson.fromJson(json, mClazz),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .create();
    }

    public static Gson getGson(FieldNamingPolicy policy) {
        return new GsonBuilder()
                .setFieldNamingPolicy(policy)
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .create();
    }
}