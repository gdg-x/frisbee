package org.gdg.frisbee.android.about;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.gdg.frisbee.android.R;

class AboutPagerAdapter extends FragmentStatePagerAdapter {
    private Context mContext;

    public AboutPagerAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mContext.getResources().getStringArray(R.array.about_tabs).length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AboutFragment();
            case 1:
                return new ContributorsFragment();
            case 2:
                return new TranslatorsFragment();
            case 3:
                return new ChangelogFragment();
            case 4:
                return new GetInvolvedFragment();
            case 5:
                return new ExtLibrariesFragment();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getStringArray(R.array.about_tabs)[position];
    }
}
