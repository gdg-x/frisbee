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
import org.joda.time.DateTimeZone;

public class TaggedEventSeries implements Parcelable {

    public static final Parcelable.Creator<TaggedEventSeries> CREATOR = new Parcelable.Creator<TaggedEventSeries>() {
        public TaggedEventSeries createFromParcel(Parcel source) {
            return new TaggedEventSeries(source);
        }

        public TaggedEventSeries[] newArray(int size) {
            return new TaggedEventSeries[size];
        }
    };

    private String mTag;
    @DrawableRes
    private int mDrawerIconResId;
    @StringRes
    private int mTitleResId;
    @StringRes
    private int mDescriptionResId;
    @DrawableRes
    private int mDefaultIconResId;
    @DrawableRes
    private int mLogoResId;
    @StyleRes
    private int mSpecialEventTheme;
    private int mDrawerId;
    @StringRes
    private int mGreetingsResId;
    @StringRes
    private int mGreetingsTitleResId;
    private DateTime mStartDate;
    private DateTime mEndDateIn;

    public TaggedEventSeries(Context context,
                             @StyleRes int specialEventTheme,
                             @NonNull String tag,
                             int drawerId,
                             DateTime startDate,
                             DateTime endDate) {

        final ContextThemeWrapper themeWrapper = new ContextThemeWrapper(context, specialEventTheme);
        final TypedArray a = themeWrapper.obtainStyledAttributes(R.styleable.SpecialEvent);

        mDrawerIconResId = a.getResourceId(R.styleable.SpecialEvent_specialEventDrawerIcon,
            R.drawable.ic_drawer_ioextended);
        mDefaultIconResId = a.getResourceId(R.styleable.SpecialEvent_specialEventDefaultIcon,
            R.drawable.ic_ioextended);
        mLogoResId = a.getResourceId(R.styleable.SpecialEvent_specialEventLogo,
            R.drawable.ic_logo_ioextended);

        mTitleResId = a.getResourceId(R.styleable.SpecialEvent_specialEventTitle,
            R.string.ioextended);
        mDescriptionResId = a.getResourceId(R.styleable.SpecialEvent_specialEventDescription,
            R.string.ioextended_description);

        mGreetingsResId =  a.getResourceId(R.styleable.SpecialEvent_specialEventGreetings,
            R.string.event_series_greetings);
        mGreetingsTitleResId =  a.getResourceId(R.styleable.SpecialEvent_specialEventGreetingsTitle,
            R.string.title_event_series_greetings);

        a.recycle();

        mTag = tag;
        mSpecialEventTheme = specialEventTheme;
        mStartDate = startDate;
        mEndDateIn = endDate;
        mDrawerId = drawerId;

    }

    private TaggedEventSeries(Parcel in) {
        mTag = in.readString();
        mDrawerIconResId = in.readInt();
        mTitleResId = in.readInt();
        mDescriptionResId = in.readInt();
        mDefaultIconResId = in.readInt();
        mLogoResId = in.readInt();
        mSpecialEventTheme = in.readInt();
        mDrawerId = in.readInt();
        mGreetingsResId = in.readInt();
        mGreetingsTitleResId = in.readInt();
        mStartDate = new DateTime(in.readLong(), DateTimeZone.UTC);
        mEndDateIn = new DateTime(in.readLong(), DateTimeZone.UTC);
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

    public int getGreetingsResId() {
        return mGreetingsResId;
    }

    public int getGreetingsTitleResId() {
        return mGreetingsTitleResId;
    }

    public DateTime getStartDate() {
        return mStartDate;
    }

    public DateTime getEndDate() {
        return mEndDateIn;
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
        dest.writeInt(mDefaultIconResId);
        dest.writeInt(mLogoResId);
        dest.writeInt(mSpecialEventTheme);
        dest.writeInt(mDrawerId);
        dest.writeInt(mGreetingsResId);
        dest.writeInt(mGreetingsTitleResId);
        dest.writeLong(mStartDate.getMillis());
        dest.writeLong(mEndDateIn.getMillis());
    }
}
