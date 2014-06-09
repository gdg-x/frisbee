package org.gdg.frisbee.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by maui on 29.05.2014.
 */
public class Gde implements Parcelable{

    private String product, name, address, email, socialUrl;
    private double lat, lng;

    public Gde(Parcel in) {
        product = in.readString();
        name = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        email = in.readString();
        socialUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(product);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(email);
        parcel.writeString(socialUrl);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Gde createFromParcel(Parcel in) {
            return new Gde(in);
        }

        public Gde[] newArray(int size) {
            return new Gde[size];
        }
    };

    public String getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
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
    public int describeContents() {
        return 0;
    }
}
