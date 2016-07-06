package org.gdg.frisbee.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Gde implements GdgPerson, Parcelable {

    private String name, address, email, socialUrl;
    private List<String> product = new ArrayList<>();
    private double lat, lng;

    protected Gde(Parcel in) {
        name = in.readString();
        address = in.readString();
        email = in.readString();
        socialUrl = in.readString();
        product = in.createStringArrayList();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(email);
        dest.writeString(socialUrl);
        dest.writeStringList(product);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Gde> CREATOR = new Creator<Gde>() {
        @Override
        public Gde createFromParcel(Parcel in) {
            return new Gde(in);
        }

        @Override
        public Gde[] newArray(int size) {
            return new Gde[size];
        }
    };

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

}
