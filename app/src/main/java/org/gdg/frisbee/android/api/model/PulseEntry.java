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

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class PulseEntry {

    private static final String FIELD_PLUS_MEMBERS = "plus_members";
    private static final String FIELD_ATTENDEES = "attendees";
    private static final String FIELD_MEETINGS = "meetings";
    private static final String FIELD_ID = "id";

    @SerializedName(FIELD_PLUS_MEMBERS)
    private int mPlusMembers;
    @SerializedName(FIELD_ATTENDEES)
    private int mAttendees;
    @SerializedName(FIELD_MEETINGS)
    private int mMeetings;
    @SerializedName(FIELD_ID)
    private String mId;

    public int getPlusMembers() {
        return mPlusMembers;
    }

    public int getAttendees() {
        return mAttendees;
    }

    public int getMeetings() {
        return mMeetings;
    }

    public String getId() {
        return mId;
    }

    public int compareTo(int mode, @NonNull PulseEntry pulseEntry) {
        switch (mode) {
            case 0:
                return (getMeetings() - pulseEntry.getMeetings()) * -1;
            case 1:
                return (getAttendees() - pulseEntry.getAttendees()) * -1;
            case 2:
                return (getPlusMembers() - pulseEntry.getPlusMembers()) * -1;
        }
        return 0;
    }
}
