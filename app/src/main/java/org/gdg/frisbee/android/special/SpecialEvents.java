package org.gdg.frisbee.android.special;

import org.gdg.frisbee.android.R;

public class SpecialEvents {
    private static final long DATE_2015_06_01_GMT_IN_MILLIS = 1433116800000L;

    private String mTag;
    private int mDrawerIconResId;
    private int mTitleResId;
    private int mDescriptionResId;
    private int mLogoResId;
    private long mEndDateInMillis;


    public SpecialEvents(String tag, int drawerIconResId, int titleResId, int descriptionResId, int logoResId, long endDateInMillis) {
        mTag = tag;
        mDrawerIconResId = drawerIconResId;
        mTitleResId = titleResId;
        mDescriptionResId = descriptionResId;
        mLogoResId = logoResId;
        mEndDateInMillis = endDateInMillis;
    }

    public static SpecialEvents getCurrent() {
        return new SpecialEvents("io-extended", R.drawable.drw_ic_ioextended, R.string.ioextended, R.string.ioextended_description,
                R.drawable.ioextended, DATE_2015_06_01_GMT_IN_MILLIS);
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

    public long getEndDateInMillis() {
        return mEndDateInMillis;
    }
}
