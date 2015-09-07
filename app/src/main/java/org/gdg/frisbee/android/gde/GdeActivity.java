package org.gdg.frisbee.android.gde;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.widget.FrameLayout;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.api.model.GdeList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.common.PlainLayoutFragment;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class GdeActivity extends GdgNavDrawerActivity {

    @Bind(R.id.pager)
    ViewPager mViewPager;

    @Bind(R.id.tabs)
    TabLayout mTabLayout;

    @Bind(R.id.content_frame)
    FrameLayout mContentLayout;

    private Handler mHandler = new Handler();

    private GdeCategoryAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gde);

        Toolbar toolbar = getActionBarToolbar();
        toolbar.setTitle(R.string.gde);

        mViewPagerAdapter = new GdeCategoryAdapter(getSupportFragmentManager());


        App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_GDE_LIST, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                GdeList directory = (GdeList) item;
                addGdes(directory);
            }

            @Override
            public void onNotFound(String key) {
                fetchGdeDirectory();
            }
        });
    }
    
    private void fetchGdeDirectory() {

        App.getInstance().getGdeDirectory().getDirectory().enqueue(new Callback<GdeList>() {
            @Override
            public void onResponse(Response<GdeList> response) {
                final GdeList directory = response.body();
                App.getInstance().getModelCache().putAsync(Const.CACHE_KEY_GDE_LIST,
                        directory,
                        DateTime.now().plusDays(4),
                        new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                addGdes(directory);
                            }
                        });
            }

            @Override
            public void onFailure(Throwable t) {
                try {
                    Snackbar snackbar = Snackbar.make(mContentLayout, R.string.fetch_gde_failed,
                            Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.alert(snackbar).show();
                } catch (IllegalStateException ignored) {
                }
                Timber.e(t, "Could'nt fetch GDE list");
            }
        });
    }

    private void addGdes(final GdeList directory) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String, GdeList> gdeMap = new HashMap<>();

                for (Gde gde : directory) {
                    if (!gdeMap.containsKey(gde.getProduct())) {
                        gdeMap.put(gde.getProduct(), new GdeList());
                    }

                    gdeMap.get(gde.getProduct()).add(gde);
                }

                mViewPagerAdapter.addMap(gdeMap);
                mViewPagerAdapter.notifyDataSetChanged();

                mViewPager.setAdapter(mViewPagerAdapter);
                mTabLayout.setupWithViewPager(mViewPager);
            }
        });
    }

    protected String getTrackedViewName() {
        return null;
    }

    public class GdeCategoryAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        private final SparseArray<WeakReference<Fragment>> mFragments = new SparseArray<>();
        private HashMap<String, GdeList> mGdeMap;

        public GdeCategoryAdapter(FragmentManager fm) {
            super(fm);
            mGdeMap = new HashMap<>();
        }

        public void addMap(Map<String, GdeList> collection) {
            mGdeMap.putAll(collection);
        }

        @Override
        public int getCount() {
            return mGdeMap.keySet().size() + 1;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return PlainLayoutFragment.newInstance(R.layout.fragment_gde_about);
            } else {
                String key = mGdeMap.keySet().toArray(new String[mGdeMap.size()])[position - 1];
                Fragment frag = GdeListFragment.newInstance(mGdeMap.get(key));
                mFragments.append(position, new WeakReference<>(frag));

                return frag;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.about);
            } else if (position > -1 && position - 1 < mGdeMap.keySet().size()) {
                String title = mGdeMap.keySet().toArray(new String[mGdeMap.size()])[position - 1];
                title = title.length() > 14 ? Utils.getUppercaseLetters(title) : title;
                return title;
            } else {
                return "";
            }
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                trackView("GDE/About");
            } else {
                String key = mGdeMap.keySet().toArray(new String[mGdeMap.size()])[position - 1];
                trackView("GDE/" + key);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {
        }

    }
}
