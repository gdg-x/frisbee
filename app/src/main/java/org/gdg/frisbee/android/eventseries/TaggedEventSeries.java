package org.gdg.frisbee.android.eventseries;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.view.ContextThemeWrapper;

import org.gdg.frisbee.android.R;
import org.joda.time.DateTime;

public class TaggedEventSeries implements Parcelable {

    private String mTag;
    private int mDrawerIconResId;
    private int mTitleResId;
    private int mDescriptionResId;
    private int mDefaultIconResId;
    private int mLogoResId;
    private int mSpecialEventTheme;
    private int mDrawerId;
    private DateTime mStartDateInMillis;
    private DateTime mEndDateInMillis;

    public TaggedEventSeries(Context context,
                             @StyleRes int specialEventTheme,
                             @NonNull String tag,
                             int drawerId,
                             DateTime startDateInMillis,
                             DateTime endDateInMillis) {

        final ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, specialEventTheme);
        final TypedArray a = themeWrapper.obtainStyledAttributes(R.styleable.SpecialEvent);

        mDrawerIconResId = a.getResourceId(R.styleable.SpecialEvent_specialEventDrawerIcon, R.drawable.ic_drawer_ioextended);
        mDefaultIconResId = a.getResourceId(R.styleable.SpecialEvent_specialEventDefaultIcon, R.drawable.ic_ioextended);
        mLogoResId = a.getResourceId(R.styleable.SpecialEvent_specialEventLogo, R.drawable.ic_logo_ioextended);

        mTitleResId = a.getResourceId(R.styleable.SpecialEvent_specialEventTitle, R.string.ioextended);
        mDescriptionResId = a.getResourceId(R.styleable.SpecialEvent_specialEventDescription, R.string.ioextended_description);

        a.recycle();

        mTag = tag;
        mSpecialEventTheme = specialEventTheme;
        mStartDateInMillis = startDateInMillis;
        mEndDateInMillis = endDateInMillis;
        mDrawerId = drawerId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public TaggedEventSeries(@NonNull String tag,
                             @DrawableRes int drawerIconResId,
                             @StringRes int titleResId,
                             @StringRes int descriptionResId,
                             @DrawableRes int defaultIconResId,
                             @DrawableRes int logoResId,
                             @StyleRes int specialEventTheme,
                             int drawerId,
                             DateTime startDateInMillis,
                             DateTime endDateInMillis) {
        mTag = tag;
        mDrawerIconResId = drawerIconResId;
        mTitleResId = titleResId;
        mDescriptionResId = descriptionResId;
        mDefaultIconResId = defaultIconResId;
        mLogoResId = logoResId;
        mSpecialEventTheme = specialEventTheme;
        mDrawerId = drawerId;
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

    public int getDefaultIconResId() {
        return mDefaultIconResId;
    }

    public int getLogoResId() {
        return mLogoResId;
    }

    public int getSpecialEventTheme() {
        return mSpecialEventTheme;
    }

    public int getDrawerId() {
        return mDrawerId;
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
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mTag);
        dest.writeInt(mDrawerIconResId);
        dest.writeInt(mTitleResId);
        dest.writeInt(mDescriptionResId);
        dest.writeInt(mDefaultIconResId);
        dest.writeInt(mLogoResId);
        dest.writeInt(mSpecialEventTheme);
        dest.writeInt(mDrawerId);
        dest.writeLong(mStartDateInMillis.getMillis());
        dest.writeLong(mEndDateInMillis.getMillis());
    }

    private TaggedEventSeries(@NonNull Parcel in) {
        mTag = in.readString();
        mDrawerIconResId = in.readInt();
        mTitleResId = in.readInt();
        mDescriptionResId = in.readInt();
        mDefaultIconResId = in.readInt();
        mLogoResId = in.readInt();
        mSpecialEventTheme = in.readInt();
        mDrawerId = in.readInt();
        mStartDateInMillis = new DateTime(in.readLong());
        mEndDateInMillis = new DateTime(in.readLong());
    }

    public static final Parcelable.Creator<TaggedEventSeries> CREATOR = new Parcelable.Creator<TaggedEventSeries>() {
        @NonNull
        public TaggedEventSeries createFromParcel(@NonNull Parcel source) {
            return new TaggedEventSeries(source);
        }

        @NonNull
        public TaggedEventSeries[] newArray(int size) {
            return new TaggedEventSeries[size];
        }
    };
}
