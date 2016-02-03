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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.VisibleForTesting;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Chapter implements Comparable<Chapter>, Parcelable {
    public static final Creator<Chapter> CREATOR = new Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };
    private String status, city, name, state;
    private Country country;
    @SerializedName("_id")
    private String gplusId;
    private ArrayList<String> organizers;
    private Geo geo;
    private String shortName;

    public Chapter() {
        name = "";
        gplusId = "";
        organizers = new ArrayList<>();
    }

    public Chapter(String name, String gplusId) {
        this.name = name;
        this.gplusId = gplusId;
        organizers = new ArrayList<>();
    }

    protected Chapter(Parcel in) {
        name = in.readString();
        status = in.readString();
        city = in.readString();
        gplusId = in.readString();
        state = in.readString();
        country = in.readParcelable(Country.class.getClassLoader());
        organizers = in.createStringArrayList();
        geo = in.readParcelable(Geo.class.getClassLoader());
        shortName = in.readString();
    }

    public ArrayList<String> getOrganizers() {
        return organizers;
    }

    public String getStatus() {
        return status;
    }

    public String getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public String getGplusId() {
        return gplusId;
    }

    public String getState() {
        return state;
    }

    public Country getCountry() {
        return country;
    }

    public Geo getGeo() {
        return geo;
    }

    @VisibleForTesting
    public void setGeo(Geo geo) {
        this.geo = geo;
    }

    @Override
    public String toString() {
        return getShortName();
    }

    public String getShortName() {
        if (shortName == null) {
            shortName = name.replaceAll("GDG ", "").trim();
        }
        return shortName;
    }

    @Override
    public int compareTo(Chapter o) {
        return getShortName().compareTo(o.getShortName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(status);
        dest.writeString(city);
        dest.writeString(gplusId);
        dest.writeString(state);
        dest.writeParcelable(country, flags);
        dest.writeStringList(organizers);
        dest.writeParcelable(geo, flags);
        dest.writeString(shortName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof Chapter) {
            Chapter other = (Chapter) o;

            return other.getGplusId().equals(getGplusId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (getGplusId() != null) {
            return getGplusId().hashCode();
        } else {
            return super.hashCode();
        }
    }
}
