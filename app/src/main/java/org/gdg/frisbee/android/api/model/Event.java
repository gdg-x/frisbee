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

package org.gdg.frisbee.android.api.model;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;


public class Event implements GdgResponse, SimpleEvent {

    private static final String FIELD_TIMEZONE_NAME = "timezoneName";
    private static final String FIELD_TEMPORAL_RELATION = "temporalRelation";
    private static final String FIELD_START = "start";
    private static final String FIELD_ID = "id";
    private static final String FIELD_ICON_URL = "iconUrl";
    private static final String FIELD_GROUP = "group";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_END = "end";
    private static final String FIELD_LOCATION = "location";
    private static final String FIELD_LINK = "link";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String FIELD_GPLUS_EVENT_LINK = "gPlusEventLink";

    @SerializedName(FIELD_START)
    private DateTime mStart;
    @SerializedName(FIELD_END)
    private DateTime mEnd;
    @SerializedName(FIELD_TIMEZONE_NAME)
    private String mTimezoneName;
    @SerializedName(FIELD_DESCRIPTION)
    private String mDescription;
    @SerializedName(FIELD_LOCATION)
    private String mLocation;
    @SerializedName(FIELD_TEMPORAL_RELATION)
    private String mTemporalRelation;
    @SerializedName(FIELD_TITLE)
    private String mTitle;
    @SerializedName(FIELD_LINK)
    private String mLink;
    @SerializedName(FIELD_ICON_URL)
    private String mIconUrl;
    @SerializedName(FIELD_ID)
    private String mId;
    @SerializedName(FIELD_GROUP)
    private String mGroup;
    @SerializedName(FIELD_GPLUS_EVENT_LINK)
    private String mGplusEventLink;

    @Override
    public DateTime getStart() {
        return mStart;
    }

    @Override
    public DateTime getEnd() {
        return mEnd;
    }

    public String getLocation() {
        return mLocation;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getIconUrl() {
        return mIconUrl;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getGPlusEventLink() {
        return mGplusEventLink;
    }

    public String getDescription() {
        return mDescription;
    }


    @Override
    public String getLink() {
        return mLink;
    }
}
