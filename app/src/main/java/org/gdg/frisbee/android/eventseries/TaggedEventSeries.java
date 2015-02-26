package org.gdg.frisbee.android.eventseries;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import org.joda.time.DateTime;

public class TaggedEventSeries implements Parcelable {

    private String mTag;
    private int mDrawerIconResId;
    private int mTitleResId;
    private int mDescriptionResId;
    private int mLogoResId;
    private int mColorPrimary;
    private DateTime mStartDateInMillis;
    private DateTime mEndDateInMillis;

    public TaggedEventSeries(String tag, 
                             @DrawableRes int drawerIconResId, 
                             @StringRes int titleResId, 
                             @StringRes int descriptionResId, 
                             @DrawableRes int logoResId,
                             @ColorRes int colorPrimary,
                             DateTime startDateInMillis,
                             DateTime endDateInMillis) {
        mTag = tag;
        mDrawerIconResId = drawerIconResId;
        mTitleResId = titleResId;
        mDescriptionResId = descriptionResId;
        mLogoResId = logoResId;
        mColorPrimary = colorPrimary;
        mStartDateInMillis = startDateInMillis;
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

    public DateTime getStartDateInMillis() {
        return mStartDateInMillis;
    }

    public DateTime getEndDateInMillis() {
        return mEndDateInMillis;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTag);
        dest.writeInt(mDrawerIconResId);
        dest.writeInt(mTitleResId);
        dest.writeInt(mDescriptionResId);
        dest.writeInt(mLogoResId);
        dest.writeInt(mColorPrimary);
        dest.writeLong(mStartDateInMillis.getMillis());
        dest.writeLong(mEndDateInMillis.getMillis());
    }

    private TaggedEventSeries(Parcel in) {
        mTag = in.readString();
        mDrawerIconResId = in.readInt();
        mTitleResId = in.readInt();
        mDescriptionResId = in.readInt();
        mLogoResId = in.readInt();
        mColorPrimary = in.readInt();
        mStartDateInMillis = new DateTime(in.readLong());
        mEndDateInMillis = new DateTime(in.readLong());
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
