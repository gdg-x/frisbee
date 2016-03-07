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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Directory implements GdgResponse, Parcelable {

    public static final Creator<Directory> CREATOR = new Creator<Directory>() {
        @Override
        public Directory createFromParcel(Parcel in) {
            return new Directory(in);
        }

        @Override
        public Directory[] newArray(int size) {
            return new Directory[size];
        }
    };
    @SerializedName("items")
    private ArrayList<Chapter> groups;

    public Directory() {
        groups = new ArrayList<>();
    }

    protected Directory(Parcel in) {
        groups = in.createTypedArrayList(Chapter.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(groups);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<Chapter> getGroups() {
        return groups;
    }

    public Chapter getGroupById(String chapterId) {
        for (Chapter group : groups) {
            if (group.getGplusId().equals(chapterId)) {
                return group;
            }
        }
        return null;
    }
}
