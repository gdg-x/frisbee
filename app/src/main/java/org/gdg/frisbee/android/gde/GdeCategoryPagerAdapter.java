package org.gdg.frisbee.android.gde;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.PlainLayoutFragment;
import org.gdg.frisbee.android.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.List;

class GdeCategoryPagerAdapter extends FragmentStatePagerAdapter {
    private final SparseArray<WeakReference<Fragment>> mFragments = new SparseArray<>();
    private final String titleAbout;
    private final List<GdeCategory> gdeCategoryList;

    GdeCategoryPagerAdapter(FragmentManager fm, String titleAbout, List<GdeCategory> gdeCategoryList) {
        super(fm);
        this.titleAbout = titleAbout;
        this.gdeCategoryList = gdeCategoryList;
    }

    @Override
    public int getCount() {
        return gdeCategoryList.size() + 1;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return PlainLayoutFragment.newInstance(R.layout.fragment_gde_about);
        } else {
            GdeCategory gdeCategory = gdeCategoryList.get(position - 1);
            Fragment frag = GdeListFragment.newInstance(gdeCategory.getGdeList());
            mFragments.append(position, new WeakReference<>(frag));

            return frag;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return titleAbout;
        } else {
            String title = gdeCategoryList.get(position - 1).getCategory();
            return title.length() > 14 ? Utils.getUppercaseLetters(title) : title;
        }
    }

}
