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

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api.model
 * <p/>
 * User: maui
 * Date: 21.04.13
 * Time: 22:27
 */
public class Chapter implements Comparable<Chapter>, Parcelable {
    private String status, city, name, state;

    private Country country;

    @SerializedName("_id")
    private String gplusId;

    private ArrayList<String> organizers;

    private Geo geo;

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

    public Chapter(Parcel in) {
        name = in.readString();
        status = in.readString();
        city = in.readString();
        gplusId = in.readString();
        state = in.readString();
        country = in.readParcelable(Country.class.getClassLoader());
        geo = in.readParcelable(Geo.class.getClassLoader());

        organizers = new ArrayList<>();
        in.readStringList(organizers);
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

    @Override
    public String toString() {
        return getShortName();
    }

    public String getShortName(){
        return name.replaceAll("GDG ", "");
    }

    @Override
    public int compareTo(Chapter o) {
        return name.compareTo(o.getName());
    }

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(status);
        parcel.writeString(city);
        parcel.writeString(gplusId);
        parcel.writeString(state);
        parcel.writeParcelable(country, 0);
        parcel.writeParcelable(geo,0);
        parcel.writeStringList(organizers);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

    @Override
    public boolean equals(Object o) {
       if(o == null)
           return false;

        if(o instanceof Chapter) {
            Chapter other = (Chapter)o;

            return other.getGplusId().equals(getGplusId());
        }

        return false;
    }
}
