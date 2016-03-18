package org.gdg.frisbee.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Gde implements GdgPerson, Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Gde createFromParcel(Parcel in) {
            return new Gde(in);
        }

        public Gde[] newArray(int size) {
            return new Gde[size];
        }
    };
    private String name, address, email, socialUrl;
    private List<String> product = new ArrayList<>();
    private double lat, lng;

    private Gde(Parcel in) {
        in.readStringList(product);
        name = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        email = in.readString();
        socialUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(product);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(email);
        parcel.writeString(socialUrl);
    }

    public List<String> getProduct() {
        return product;
    }


    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getEmail() {
        return email;
    }

    public String getSocialUrl() {
        return socialUrl;
    }

    @Override
    public String getPrimaryText() {
        return name;
    }

    @Override
    public String getSecondaryText() {
        return address;
    }

    @Override
    public String getUrl() {
        return socialUrl;
    }

    @Override
    public String getImageUrl() {
        return socialUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
