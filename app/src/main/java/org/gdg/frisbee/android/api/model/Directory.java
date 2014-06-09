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
 * Time: 22:30
 */
public class Directory implements GdgResponse, Parcelable {

    @SerializedName("items")
    private ArrayList<Chapter> groups;

    public Directory() {
        groups = new ArrayList<Chapter>();
    }

    public Directory(Parcel in) {
        groups = new ArrayList<Chapter>();
        in.readTypedList(groups, Chapter.CREATOR);
    }

    public ArrayList<Chapter> getGroups() {
        return groups;
    }

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(groups);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Chapter createFromParcel(Parcel in) {
            return new Chapter(in);
        }

        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

    public Chapter getGroupById(String chapterId) {
        for (Chapter group: groups){
            if (group.getGplusId().equals(chapterId)){
                return group;
            }
        }
        return null;
    }
}
