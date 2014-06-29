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

import android.net.TrafficStats;
import android.os.Build;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HurlStack;
import com.google.android.gms.analytics.HitBuilders;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api
 * <p/>
 * User: maui
 * Date: 27.05.13
 * Time: 02:57
 */
public class OkStack extends HurlStack {

    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        OkUrlFactory factory = new OkUrlFactory(client);
        SSLContext sslContext;
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { new GdgTrustManager(App.getInstance().getApplicationContext()) };

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
        client.setSslSocketFactory(sslContext.getSocketFactory());
        return factory.open(url);
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        long startTime = System.currentTimeMillis();
        if(Const.DEVELOPER_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TrafficStats.setThreadStatsTag(0xF00D);
            try {
                HttpResponse res = super.performRequest(request, additionalHeaders);
                App.getInstance().getTracker().send(new HitBuilders.TimingBuilder()
                        .setCategory("net")
                        .setValue(System.currentTimeMillis()-startTime)
                        .setVariable("okhttp")
                        .setLabel("okhttp")
                        .build());
                return res;
            } finally {
                TrafficStats.clearThreadStatsTag();
            }
        } else {
            HttpResponse response = super.performRequest(request, additionalHeaders);
            App.getInstance().getTracker().send(new HitBuilders.TimingBuilder()
                    .setCategory("net")
                    .setValue(System.currentTimeMillis()-startTime)
                    .setVariable("okhttp")
                    .setLabel("okhttp")
                    .build());
            return response;
        }
    }
}
