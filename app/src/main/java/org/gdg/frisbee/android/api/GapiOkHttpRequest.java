/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.gdg.frisbee.android.api;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.Preconditions;
import org.gdg.frisbee.android.app.App;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class GapiOkHttpRequest extends LowLevelHttpRequest {
    private final HttpURLConnection connection;

    /**
     * @param connection HTTP URL connection
     */
    GapiOkHttpRequest(HttpURLConnection connection) {
        this.connection = connection;
        connection.setInstanceFollowRedirects(true);
    }

    @Override
    public void addHeader(String name, String value) {
        connection.addRequestProperty(name, value);
    }

    @Override
    public void setTimeout(int connectTimeout, int readTimeout) {
        connection.setReadTimeout(readTimeout);
        connection.setConnectTimeout(connectTimeout);
    }

    @Override
    public LowLevelHttpResponse execute() throws IOException {
        HttpURLConnection connection = this.connection;
        long startTime = System.currentTimeMillis();
        // write content
        if (getStreamingContent() != null) {
            String contentType = getContentType();
            if (contentType != null) {
                addHeader("Content-Type", contentType);
            }
            String contentEncoding = getContentEncoding();
            if (contentEncoding != null) {
                addHeader("Content-Encoding", contentEncoding);
            }
            long contentLength = getContentLength();
            if (contentLength >= 0) {
                addHeader("Content-Length", Long.toString(contentLength));
            }
            String requestMethod = connection.getRequestMethod();
            if ("POST".equals(requestMethod) || "PUT".equals(requestMethod)) {
                connection.setDoOutput(true);
                // see http://developer.android.com/reference/java/net/HttpURLConnection.html
                if (contentLength >= 0 && contentLength <= Integer.MAX_VALUE) {
                    connection.setFixedLengthStreamingMode((int) contentLength);
                } else {
                    connection.setChunkedStreamingMode(0);
                }
                OutputStream out = connection.getOutputStream();
                try {
                    getStreamingContent().writeTo(out);
                } finally {
                    out.close();
                }
            } else {
                // cannot call setDoOutput(true) because it would change a GET method to POST
                // for HEAD, OPTIONS, DELETE, or TRACE it would throw an exceptions
                Preconditions.checkArgument(
                        contentLength == 0, "%s with non-zero content length is not supported", requestMethod);
            }
        }
        // connect
        boolean successfulConnection = false;
        try {
            connection.connect();
            GapiOkResponse response = new GapiOkResponse(connection);
            successfulConnection = true;
            App.getInstance().getTracker().sendTiming("net",System.currentTimeMillis()-startTime, "gapi", null);
            return response;
        } finally {
            if (!successfulConnection) {
                connection.disconnect();
            }
        }
    }
}
