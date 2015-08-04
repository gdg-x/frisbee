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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
        groups = new ArrayList<>();
    }

    public Directory(@NonNull Parcel in) {
        groups = new ArrayList<>();
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
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeTypedList(groups);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @NonNull
        public Chapter createFromParcel(@NonNull Parcel in) {
            return new Chapter(in);
        }

        @NonNull
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

    @Nullable
    public Chapter getGroupById(String chapterId) {
        for (Chapter group : groups) {
            if (group.getGplusId().equals(chapterId)) {
                return group;
            }
        }
        return null;
    }
}
