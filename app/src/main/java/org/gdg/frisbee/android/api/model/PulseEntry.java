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

package org.gdg.frisbee.android.api.model;

public class PulseEntry {
    private int plusMembers, meetings, attendees;
    private String id;

    public int getPlusMembers() {
        return plusMembers;
    }

    public int getMeetings() {
        return meetings;
    }

    public int getAttendees() {
        return attendees;
    }

    public String getId() {
        return id;
    }

    public int compareTo(int mode, PulseEntry pulseEntry) {
        switch(mode) {
            case 0:
                return (getMeetings()-pulseEntry.getMeetings())*-1;
            case 1:
                return (getAttendees()-pulseEntry.getAttendees())*-1;
            case 2:
                return (getPlusMembers()-pulseEntry.getPlusMembers())*-1;
        }
        return 0;
    }
}
