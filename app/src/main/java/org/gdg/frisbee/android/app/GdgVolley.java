/*
 * Copyright 2013-2015 The GDG Frisbee Project
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

package org.gdg.frisbee.android.app;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.gdg.frisbee.android.api.GdgStack;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api
 * <p/>
 * User: maui
 * Date: 26.05.13
 * Time: 19:25
 */
public class GdgVolley {

    private static GdgVolley mInstance;
    public static GdgVolley getInstance() {
        return mInstance;
    }

    private RequestQueue mRequestQueue;

    static void init(Context ctx) {
        mInstance = new GdgVolley(ctx);
    }

    private GdgVolley(Context ctx) {
        mRequestQueue = Volley.newRequestQueue(ctx, new GdgStack());
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }
}
