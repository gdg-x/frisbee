package org.gdg.frisbee.android.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.widget.Toast;
import butterknife.InjectView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.viewpagerindicator.TitlePageIndicator;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GdeDirectory;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.api.model.GdeList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.fragment.GdeListFragment;
import org.gdg.frisbee.android.fragment.GdlListFragment;
import org.gdg.frisbee.android.fragment.PlainLayoutFragment;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;
import timber.log.Timber;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maui on 28.05.2014.
 */
public class GdeActivity extends GdgNavDrawerActivity {

    private static String LOG_TAG = "GDG-GdeActivity";

    private GdeDirectory mGdeDirectory;

    @InjectView(R.id.pager)
    ViewPager mViewPager;

    @InjectView(R.id.titles)
    TitlePageIndicator mIndicator;

    private Handler mHandler = new Handler();

    private GdeCategoryAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gde);

        getSupportActionBar().setLogo(R.drawable.ic_gde_logo_wide);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPagerAdapter = new GdeCategoryAdapter(this, getSupportFragmentManager());

        mGdeDirectory = new GdeDirectory();
        final ApiRequest mFetchGdesTask = mGdeDirectory.getDirectory(new Response.Listener<GdeList>() {
            @Override
            public void onResponse(final GdeList directory) {
                App.getInstance().getModelCache().putAsync("gde_map", directory, DateTime.now().plusDays(4), new ModelCache.CachePutListener() {
                    @Override
                    public void onPutIntoCache() {
                        addGdes(directory);
                    }
                });

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Crouton.makeText(GdeActivity.this, getString(R.string.fetch_gde_failed), Style.ALERT).show();
                Timber.e("Could'nt fetch GDE list", volleyError);
            }
        });

        App.getInstance().getModelCache().getAsync("gde_map", new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                GdeList directory = (GdeList) item;
                addGdes(directory);
            }

            @Override
            public void onNotFound(String key) {
                mFetchGdesTask.execute();
            }
        });
    }


    private void addGdes(final GdeList directory) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                HashMap<String, GdeList> gdeMap = new HashMap<>();

                for(Gde gde : directory) {
                    if(!gdeMap.containsKey(gde.getProduct())) {
                        gdeMap.put(gde.getProduct(), new GdeList());
                    }

                    gdeMap.get(gde.getProduct()).add(gde);
                }

                mViewPagerAdapter.addMap(gdeMap);
                mViewPagerAdapter.notifyDataSetChanged();

                mViewPager.setAdapter(mViewPagerAdapter);
                mIndicator.setViewPager(mViewPager);
            }
        });
    }

    protected String getTrackedViewName() {
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume()");

        //mIndicator.setOnPageChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Timber.d("onPause()");
    }

    public class GdeCategoryAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {
        private Context mContext;

        private HashMap<String, GdeList> mGdeMap;
        private final SparseArray<WeakReference<Fragment>> mFragments
                = new SparseArray<WeakReference<Fragment>>();

        public GdeCategoryAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
            mGdeMap = new HashMap<String, GdeList>();
        }

        public void addMap(Map<String, GdeList> collection) {
            mGdeMap.putAll(collection);
        }

        @Override
        public int getCount() {
            return mGdeMap.keySet().size()+1;
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                return PlainLayoutFragment.newInstance(R.layout.fragment_gde_about);
            } else {
                String key = mGdeMap.keySet().toArray(new String[0])[position-1];
                Fragment frag = GdeListFragment.newInstance(mGdeMap.get(key), position == mViewPager.getCurrentItem());
                mFragments.append(position, new WeakReference<Fragment>(frag));

                return frag;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position == 0) {
                return getString(R.string.about);
            } else if(position > -1 && position-1 < mGdeMap.keySet().size()) {
                String title = mGdeMap.keySet().toArray(new String[0])[position-1];
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
            } else  {
                String key = mGdeMap.keySet().toArray(new String[0])[position-1];
                trackView("GDE/" + key);

                WeakReference<Fragment> ref = mFragments.get(position-1);
                Fragment frag = null != ref ? ref.get() : null;

                // We need to notify the fragment that it is selected
                if (frag != null && frag instanceof Listener) {
                    ((Listener) frag).onPageSelected();
                }
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
