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

import android.os.Build;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api
 * <p/>
 * User: maui
 * Date: 27.05.13
 * Time: 03:14
 */
public class GapiTransportChooser {
    public static HttpTransport newCompatibleTransport() {
        return isGingerbreadOrHigher() ? new GapiOkTransport() : new ApacheHttpTransport();
    }

    /** Returns whether the SDK version is Gingerbread (2.3) or higher. */
    public static boolean isGingerbreadOrHigher() {
        return Build.VERSION.SDK_INT >= 9;
    }
}
