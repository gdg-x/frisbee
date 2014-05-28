package org.gdg.frisbee.android.activity;

import android.content.Context;
import android.os.Bundle;
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
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.fragment.GdeListFragment;
import org.gdg.frisbee.android.fragment.GdlListFragment;
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

    private GdeCategoryAdapter mViewPagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gdl);

        getSupportActionBar().setLogo(R.drawable.ic_gde_logo_wide);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mViewPagerAdapter = new GdeCategoryAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mIndicator.setViewPager(mViewPager);

        mGdeDirectory = new GdeDirectory();
        final ApiRequest mFetchGdesTask = mGdeDirectory.getDirectory(new Response.Listener<HashMap<String, ArrayList<Gde>>>() {
            @Override
            public void onResponse(final HashMap<String, ArrayList<Gde>> directory) {
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
                HashMap<String, ArrayList<Gde>> directory = (HashMap<String, ArrayList<Gde>>) item;
                addGdes(directory);
            }

            @Override
            public void onNotFound(String key) {
                mFetchGdesTask.execute();
            }
        });
    }


    private void addGdes(HashMap<String, ArrayList<Gde>> directory) {
        mViewPagerAdapter.addMap(directory);
        mViewPagerAdapter.notifyDataSetChanged();
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

        private HashMap<String, ArrayList<Gde>> mGdeMap;
        private final SparseArray<WeakReference<Fragment>> mFragments
                = new SparseArray<WeakReference<Fragment>>();

        public GdeCategoryAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
            mGdeMap = new HashMap<String, ArrayList<Gde>>();
        }

        public void addMap(Map<String, ArrayList<Gde>> collection) {
            mGdeMap.putAll(collection);
        }

        @Override
        public int getCount() {
            return mGdeMap.keySet().size();
        }

        @Override
        public Fragment getItem(int position) {
            String key = mGdeMap.keySet().toArray(new String[0])[position];
            Fragment frag = GdeListFragment.newInstance(mGdeMap.get(mGdeMap.get(key)), position == mViewPager.getCurrentItem());
            mFragments.append(position, new WeakReference<Fragment>(frag));

            return frag;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if(position > 0 && position < mGdeMap.keySet().size()) {
                return mGdeMap.keySet().toArray(new String[0])[position];
            } else {
                return "";
            }
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {
        }

        @Override
        public void onPageSelected(int i) {
            String key = mGdeMap.keySet().toArray(new String[0])[i];
            trackView("GDE/" + key);

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
