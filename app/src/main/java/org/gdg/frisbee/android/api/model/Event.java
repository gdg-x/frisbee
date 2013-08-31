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

import java.util.ArrayList;

import org.joda.time.DateTime;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api.model
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 22:44
 */
public class Event implements GdgResponse, SimpleEvent {
    private ArrayList<String> className;
    private DateTime start, end;
    private int participantsCount;
    private String timezoneName, description, location, title, link, iconUrl, id, gPlusEventLink;

    public Event() {
        className = new ArrayList<String>();
    }

    public ArrayList<String> getClassName() {
        return className;
    }

    @Override
    public DateTime getStart() {
        return start;
    }

    @Override
    public DateTime getEnd() {
        return end;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getIconUrl() {
        return iconUrl;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getGPlusEventLink() {
        return gPlusEventLink;
    }

    public String getDescription() {
        return description;
    }

    public int getParticipantsCount() {
        return participantsCount;
    }

    @Override
    public String getLink() {
        return link;
    }
}
