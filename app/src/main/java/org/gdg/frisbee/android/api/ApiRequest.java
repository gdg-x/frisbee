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
import org.gdg.frisbee.android.app.GdgVolley;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 19:41
 * To change this template use File | Settings | File Templates.
 */
public class ApiRequest {
    private Request mRequest;

    public ApiRequest(Request request) {
        mRequest = request;
    }

    public void execute() {
        GdgVolley.getInstance().getRequestQueue().add(mRequest);
    }
}