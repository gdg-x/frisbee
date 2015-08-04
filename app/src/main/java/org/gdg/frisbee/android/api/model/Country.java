package org.gdg.frisbee.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by maui on 28.05.2014.
 */
public class Country implements Parcelable {

    @SerializedName("_id")
    private String isoCode;

    private String name;

    public Country() {
        name = "";
        isoCode = "";
    }

    public Country(String isoCode, String name) {
        this.name = name;
        this.isoCode = isoCode;
    }

    public Country(@NonNull Parcel in) {
        name = in.readString();
        isoCode = in.readString();
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(isoCode);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @NonNull
        public Country createFromParcel(@NonNull Parcel in) {
            return new Country(in);
        }

        @NonNull
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
}
