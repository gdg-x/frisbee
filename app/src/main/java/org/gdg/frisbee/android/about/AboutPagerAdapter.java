package org.gdg.frisbee.android.about;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.gdg.frisbee.android.R;

class AboutPagerAdapter extends FragmentStatePagerAdapter {

    private final String[] pageTitles;

    AboutPagerAdapter(FragmentManager fm, Resources resources) {
        super(fm);
        pageTitles = resources.getStringArray(R.array.about_tabs);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return pageTitles.length;
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
            default:
                throw new IllegalStateException("Unknown page in About Screen.");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pageTitles[position];
    }
}
