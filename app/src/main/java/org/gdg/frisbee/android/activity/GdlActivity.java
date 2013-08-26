package org.gdg.frisbee.android.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;

import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;

import java.lang.ref.WeakReference;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.fragment.GdlListFragment;

import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 20:02
 * To change this template use File | Settings | File Templates.
 */
public class GdlActivity extends GdgNavDrawerActivity {

    private static String LOG_TAG = "GDG-GdlActivity";

    @InjectView(R.id.pager)
    private ViewPager mViewPager;

    @InjectView(R.id.titles)
    private TitlePageIndicator mIndicator;

    private SharedPreferences mPreferences;
    private GdlCategoryAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.

        setContentView(R.layout.activity_gdl);

        getSupportActionBar().setLogo(R.drawable.ic_gdl_logo_wide);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPagerAdapter = new GdlCategoryAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mIndicator.setViewPager(mViewPager);
    }

    private void trackViewPagerPage(int position) {
        Log.d(LOG_TAG, "trackViewPagerPage()");
        App.getInstance().getTracker().sendView(String.format("/GDL/%s", getResources().getStringArray(R.array.gdl_catgories)[position]));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume()");

        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onPageSelected(int i) {
                Log.d(LOG_TAG, "onPageSelected()");
                mViewPagerAdapter.onPageSelected(i);
                trackViewPagerPage(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        trackViewPagerPage(mViewPager.getCurrentItem());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }

    public class GdlCategoryAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        private Context mContext;
        private final SparseArray<WeakReference<Fragment>> mFragments
                = new SparseArray<WeakReference<Fragment>>();

        public GdlCategoryAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
        }

        @Override
        public int getCount() {
            return mContext.getResources().getStringArray(R.array.gdl_catgories).length;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment frag = GdlListFragment.newInstance(mContext.getResources().getStringArray(R.array.gdl_catgories_url)[position], position == mViewPager.getCurrentItem());
            mFragments.append(position, new WeakReference<Fragment>(frag));

            return frag;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mContext.getResources().getStringArray(R.array.gdl_catgories)[position];
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int i) {
            WeakReference<Fragment> ref = mFragments.get(i);
            Fragment frag = null != ref ? ref.get() : null;

            // We need to notify the fragment that it is selected
            if (frag != null && frag instanceof Listener) {
                ((Listener) frag).onPageSelected();
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }

    }

    public interface Listener {
        /**
         * Called when the item has been selected in the ViewPager.
         */
        void onPageSelected();
    }
}
