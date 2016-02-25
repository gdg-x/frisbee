package org.gdg.frisbee.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Country implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
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

    public Country(Parcel in) {
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(isoCode);
    }
}
