package org.gdg.frisbee.android.eventseries;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import org.joda.time.DateTime;

public class TaggedEventSeries implements Parcelable {

    private String mTag;
    private int mDrawerIconResId;
    private int mTitleResId;
    private int mDescriptionResId;
    private int mLogoResId;
    private long mSartDateInMillis;
    private long mEndDateInMillis;

    public TaggedEventSeries(String tag, 
                             @DrawableRes int drawerIconResId, 
                             @StringRes int titleResId, 
                             @StringRes int descriptionResId, 
                             @DrawableRes int logoResId,
                             long startDateInMillis,
                             long endDateInMillis) {
        mTag = tag;
        mDrawerIconResId = drawerIconResId;
        mTitleResId = titleResId;
        mDescriptionResId = descriptionResId;
        mLogoResId = logoResId;
        mSartDateInMillis = startDateInMillis;
        mEndDateInMillis = endDateInMillis;
    }

    public String getTag() {
        return mTag;
    }

    public int getDrawerIconResId() {
        return mDrawerIconResId;
    }

    public int getTitleResId() {
        return mTitleResId;
    }

    public int getDescriptionResId() {
        return mDescriptionResId;
    }

    public int getLogoResId() {
        return mLogoResId;
    }

    public long getSartDateInMillis() {
        return mSartDateInMillis;
    }

    public long getEndDateInMillis() {
        return mEndDateInMillis;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTag);
        dest.writeInt(this.mDrawerIconResId);
        dest.writeInt(this.mTitleResId);
        dest.writeInt(this.mDescriptionResId);
        dest.writeInt(this.mLogoResId);
        dest.writeLong(this.mSartDateInMillis);
        dest.writeLong(this.mEndDateInMillis);
    }

    private TaggedEventSeries(Parcel in) {
        this.mTag = in.readString();
        this.mDrawerIconResId = in.readInt();
        this.mTitleResId = in.readInt();
        this.mDescriptionResId = in.readInt();
        this.mLogoResId = in.readInt();
        this.mSartDateInMillis = in.readLong();
        this.mEndDateInMillis = in.readLong();
    }

    public static final Parcelable.Creator<TaggedEventSeries> CREATOR = new Parcelable.Creator<TaggedEventSeries>() {
        public TaggedEventSeries createFromParcel(Parcel source) {
            return new TaggedEventSeries(source);
        }

        public TaggedEventSeries[] newArray(int size) {
            return new TaggedEventSeries[size];
        }
    };
}
