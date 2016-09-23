package org.gdg.frisbee.android.onboarding;

import android.os.Parcel;
import android.os.Parcelable;

class Invite implements Parcelable {
    final String sender;

    Invite(String sender) {
        this.sender = sender;
    }

    Invite(Parcel in) {
        sender = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sender);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Invite> CREATOR = new Creator<Invite>() {
        @Override
        public Invite createFromParcel(Parcel in) {
            return new Invite(in);
        }

        @Override
        public Invite[] newArray(int size) {
            return new Invite[size];
        }
    };
}
