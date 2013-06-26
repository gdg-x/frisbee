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

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api.model
 * <p/>
 * User: maui
 * Date: 21.04.13
 * Time: 22:28
 */
public class Geo implements Parcelable {
    private double lat, lng;

    public Geo() {

    }

    public Geo(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Geo createFromParcel(Parcel in) {
            return new Geo(in);
        }

        public Geo[] newArray(int size) {
            return new Geo[size];
        }
    };
}
