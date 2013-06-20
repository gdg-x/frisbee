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

package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.util.Log;
import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.youtube.YouTube;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GapiTransportChooser;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 13.06.13
 * Time: 02:54
 */
public class YoutubeVideoListFragment extends GdgListFragment {

    private static final String LOG_TAG = "GDG-YoutubeVideoListFragment";

    final HttpTransport mTransport = GapiTransportChooser.newCompatibleTransport();
    final JsonFactory mJsonFactory = new GsonFactory();

    private YouTube mYoutube;

    public static YoutubeVideoListFragment newInstance(String channelId) {
        YoutubeVideoListFragment fragment = new YoutubeVideoListFragment();
        Bundle arguments = new Bundle();
        arguments.putString("channel_id", channelId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated()");

        mYoutube = new YouTube.Builder(mTransport, mJsonFactory, null).setGoogleClientRequestInitializer(new CommonGoogleJsonClientRequestInitializer(getString(R.string.ip_simple_api_access_key))).build();
    }

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }
}
