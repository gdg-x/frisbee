/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.api;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.OkUrlFactory;

public class GapiOkTransport extends HttpTransport {

    /**
     * All valid request methods as specified in {@link HttpURLConnection#setRequestMethod}, sorted in
     * ascending alphabetical order.
     */
    private static final String[] SUPPORTED_METHODS = {HttpMethods.DELETE,
        HttpMethods.GET,
        HttpMethods.HEAD,
        HttpMethods.OPTIONS,
        HttpMethods.POST,
        HttpMethods.PUT,
        HttpMethods.TRACE
    };
    static {
        Arrays.sort(SUPPORTED_METHODS);
    }

    private final OkHttpClient okHttpClient;

    public GapiOkTransport(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public boolean supportsMethod(String method) {
        return Arrays.binarySearch(SUPPORTED_METHODS, method) >= 0;
    }

    @Override
    protected GapiOkHttpRequest buildRequest(String method, String url) throws IOException {
        Preconditions.checkArgument(supportsMethod(method), "HTTP method %s not supported", method);
        // connection with proxy settings
        URL connUrl = new URL(url);
        OkUrlFactory factory = new OkUrlFactory(okHttpClient);

        URLConnection conn = factory.open(connUrl);
        HttpURLConnection connection = (HttpURLConnection) conn;
        connection.setRequestMethod(method);

        return new GapiOkHttpRequest(connection);
    }
}
