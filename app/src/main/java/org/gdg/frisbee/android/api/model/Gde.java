package org.gdg.frisbee.android.api.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class Gde implements GdgPerson, Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @NonNull
        public Gde createFromParcel(@NonNull Parcel in) {
            return new Gde(in);
        }

        @NonNull
        public Gde[] newArray(int size) {
            return new Gde[size];
        }
    };
    private String product, name, address, email, socialUrl;
    private double lat, lng;

    public Gde(@NonNull Parcel in) {
        product = in.readString();
        name = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        email = in.readString();
        socialUrl = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(product);
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
        parcel.writeString(email);
        parcel.writeString(socialUrl);
    }

    @NonNull
    public String getProduct() {
        return product.trim();
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
    public String getPrimaryText() {
        return getName();
    }

    @Override
    public String getSecondaryText() {
        return getAddress();
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
