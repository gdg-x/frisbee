package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.games.GamesClient;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.DrawerAdapter;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import org.gdg.frisbee.android.view.ActionBarDrawerToggleCompat;

import roboguice.inject.InjectView;

public abstract class GdgNavDrawerActivity extends GdgActivity {

    @InjectView(R.id.drawer)
    protected DrawerLayout mDrawerLayout;
    @InjectView(R.id.left_drawer)
    protected ListView mDrawerContent;
    protected DrawerAdapter mDrawerAdapter;

    protected ActionBarDrawerToggleCompat mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDrawerAdapter = new DrawerAdapter(this);
        mDrawerContent.setAdapter(mDrawerAdapter);
        mDrawerContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DrawerAdapter.DrawerItem item = (DrawerAdapter.DrawerItem) mDrawerAdapter.getItem(i);

                switch (item.getTitle()) {
                    case R.string.achievements:
                        if (mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
                            getPlayServicesHelper().getGamesClient(new PlayServicesHelper.OnGotGamesClientListener() {
                                @Override
                                public void onGotGamesClient(GamesClient c) {
                                    startActivityForResult(c.getAchievementsIntent(), 0);
                                }
                            });
                        } else {
                            Toast.makeText(GdgNavDrawerActivity.this, getString(R.string.achievements_need_signin), Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.string.home_gdg:
                        startActivity(new Intent(GdgNavDrawerActivity.this, MainActivity.class));
                        break;
                    case R.string.about:
                        startActivity(new Intent(GdgNavDrawerActivity.this, AboutActivity.class));
                        break;
                    case R.string.gdl:
                        startActivity(new Intent(GdgNavDrawerActivity.this, GdlActivity.class));
                        break;
                    case R.string.pulse:
                        startActivity(new Intent(GdgNavDrawerActivity.this, PulseActivity.class));
                        break;
                    case R.string.settings:
                        startActivity(new Intent(GdgNavDrawerActivity.this, SettingsActivity.class));
                        break;
                }
            }
        });

        mDrawerToggle = new ActionBarDrawerToggleCompat(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                if (mPreferences.getBoolean(Const.SETTINGS_OPEN_DRAWER_ON_START, Const.SETTINGS_OPEN_DRAWER_ON_START_DEFAULT)) {
                    mPreferences.edit().putBoolean(Const.SETTINGS_OPEN_DRAWER_ON_START, !Const.SETTINGS_OPEN_DRAWER_ON_START_DEFAULT).apply();
                }
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(R.layout.abstract_activity_navigation_drawer);
        FrameLayout view = (FrameLayout) findViewById(R.id.content_container);
        View content = getLayoutInflater().inflate(layoutResId, null);
        view.addView(content);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPreferences.getBoolean(Const.SETTINGS_OPEN_DRAWER_ON_START, Const.SETTINGS_OPEN_DRAWER_ON_START_DEFAULT)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
    }
}
