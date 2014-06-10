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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.*;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Map;

import org.gdg.frisbee.android.api.deserializer.DateTimeDeserializer;
import org.joda.time.DateTime;


public class GsonRequest<Input, Output> extends GdgRequest<Output> {
    private final Gson mGson;
    private final Type mClazz;
    private Input mInput = null;
    private String mToken = null;

    public GsonRequest(int method,
                       String url,
                       Input input,
                       Type clazz,
                       Listener<Output> listener,
                       ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.mClazz = clazz;
        mInput = input;
        mGson = new Gson();
    }

    public GsonRequest(int method,
                       String url,
                       Input input,
                       Type clazz,
                       Listener<Output> listener,
                       ErrorListener errorListener,
                       Gson gson) {
        super(method, url, listener, errorListener);
        this.mClazz = clazz;
        mInput = input;
        mGson = gson;
    }

    public GsonRequest(int method,
                       String url,
                       Type clazz,
                       Listener<Output> listener,
                       ErrorListener errorListener) {
        super(method, url, listener, errorListener);
        this.mClazz = clazz;
        mGson = new Gson();
    }

    public GsonRequest(int method,
                       String url,
                       Type clazz,
                       Listener<Output> listener,
                       ErrorListener errorListener,
                       Gson gson) {
        super(method, url, listener, errorListener);
        this.mClazz = clazz;
        mGson = gson;
    }

    @Override
    protected void deliverResponse(Output response) {
        if(mListener != null)
            mListener.onResponse(response);
    }

    @Override
    public String getBodyContentType() {
        if(mInput != null)
            return "application/json";
        else
            return super.getBodyContentType();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if(mInput != null)
            return mGson.toJson(mInput).getBytes();
        else
            return super.getBody();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if(mToken != null)
            headers.put("Authorization","Bearer "+ mToken);

        return headers;
    }

    @Override
    protected Response<Output> parseNetworkResponse(NetworkResponse response) {
        if(mClazz.equals(Void.class)) {
            return Response.success(null,
                    HttpHeaderParser.parseCacheHeaders(response));
        }
        try {
            String json = new String(response.data, "UTF-8");
            return (Response<Output>)Response.success(mGson.fromJson(json, mClazz),
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

    public static Gson getGson(FieldNamingPolicy policy, JsonDeserializer<DateTime> dateTimeDeserializer) {
        return new GsonBuilder()
                .setFieldNamingPolicy(policy)
                .registerTypeAdapter(DateTime.class, dateTimeDeserializer)
                .create();

    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        this.mToken = token;
    }

}