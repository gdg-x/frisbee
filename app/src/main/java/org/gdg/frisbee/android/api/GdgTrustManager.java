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
import android.util.Log;
import org.gdg.frisbee.android.Const;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 23.08.13
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
public class GdgTrustManager implements X509TrustManager {

    private static final String LOG_TAG = "GDG-GdgTrustManager";

    private X509TrustManager mDefaultTrustManager;
    private X509TrustManager mLocalTrustManager;

    private X509Certificate[] acceptedIssuers;

    public GdgTrustManager(Context ctx) {
        try {
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);

            mDefaultTrustManager = findX509TrustManager(tmf);
            if (mDefaultTrustManager == null) {
                throw new IllegalStateException(
                        "Couldn't find X509TrustManager");
            }

            KeyStore localTrustStore = KeyStore.getInstance("BKS");
            try {
                InputStream in = ctx.getAssets().open("truststore.bks");
                localTrustStore.load(in, Const.TRUSTSTORE_PW.toCharArray());
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (localTrustStore == null) {
                throw new IllegalStateException(
                        "Couldn't load local KeyStore");
            }

            TrustManagerFactory localTmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            localTmf.init(localTrustStore);
            mLocalTrustManager = findX509TrustManager(localTmf);
            if (mLocalTrustManager == null) {
                throw new IllegalStateException(
                        "Couldn't find local X509TrustManager");
            }

            List<X509Certificate> allIssuers = new ArrayList<X509Certificate>();
            for (X509Certificate cert : mDefaultTrustManager
                    .getAcceptedIssuers()) {
                allIssuers.add(cert);
            }
            for (X509Certificate cert : mLocalTrustManager.getAcceptedIssuers()) {
                allIssuers.add(cert);
            }
            acceptedIssuers = allIssuers.toArray(new X509Certificate[allIssuers
                    .size()]);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private X509TrustManager findX509TrustManager(TrustManagerFactory tmf) {
        TrustManager tms[] = tmf.getTrustManagers();
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                return (X509TrustManager) tms[i];
            }
        }

        return null;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            mDefaultTrustManager.checkClientTrusted(chain, authType);
        } catch (CertificateException ce) {
            mLocalTrustManager.checkClientTrusted(chain, authType);
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        try {
            mDefaultTrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException ce) {
            mLocalTrustManager.checkServerTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return acceptedIssuers;
    }
}
