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

import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.Pulse;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface GroupDirectory {

    @GET("groups/pulse_stats/")
    Call<Pulse> getPulse();

    @GET("groups/pulse_stats/{country}/")
    Call<Pulse> getCountryPulse(@Path("country") String country);

    @GET("events/feed/json")
    Call<ArrayList<Event>> getChapterEventList(@Query("start") final int start,
                             @Query("end") final int end,
                             @Query("group") final String chapterId);
}
