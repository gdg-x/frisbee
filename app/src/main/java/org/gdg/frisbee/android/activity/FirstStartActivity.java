/*
 * Copyright 2013 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.activity;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GdgX;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.fragment.FirstStartStep1Fragment;
import org.gdg.frisbee.android.fragment.FirstStartStep2Fragment;
import org.gdg.frisbee.android.fragment.FirstStartStep3Fragment;
import org.gdg.frisbee.android.view.NonSwipeableViewPager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 29.04.13
 * Time: 14:48
 */
public class FirstStartActivity extends ActionBarActivity implements FirstStartStep1Fragment.Step1Listener, FirstStartStep2Fragment.Step2Listener, FirstStartStep3Fragment.Step3Listener {

    private static String LOG_TAG = "GDG-FirstStartActivity";

    private GoogleCloudMessaging mGcm;

    @InjectView(R.id.pager)
    NonSwipeableViewPager mViewPager;

    @InjectView(R.id.loading)
    LinearLayout mLoading;

    private SharedPreferences mPreferences;
    private Chapter mSelectedChapter;
    private FirstStartPageAdapter mViewPagerAdapter;

    private Handler mHandler = new Handler();

    private GoogleApiClient mGoogleApiClient = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate");
        setContentView(R.layout.activity_first_start);

        App.getInstance().updateLastLocation();
        mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);

        ButterKnife.inject(this);

        mViewPagerAdapter = new FirstStartPageAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mGcm = GoogleCloudMessaging.getInstance(this);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onPageSelected(int i) {
                Tracker t = App.getInstance().getTracker();
                // Set screen name.
                // Where path is a String representing the screen name.
                t.setScreenName("/FirstStart/Step"+(1+i));

                // Send a screen view.
                t.send(new HitBuilders.AppViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    private void setLoadingScreen(boolean show) {
        Animation fadeAnim;
        if(show) {
            fadeAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            fadeAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mLoading.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } else {
            fadeAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            fadeAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mLoading.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        }

        mLoading.startAnimation(fadeAnim);
    }

    @Override
    public void onConfirmedChapter(Chapter chapter) {
        mSelectedChapter = chapter;
        mPreferences.edit()
                .putString(Const.SETTINGS_HOME_GDG, chapter.getGplusId())
                .apply();

        if(mGoogleApiClient == null) {
            mViewPager.setCurrentItem(1, true);
        } else {
            mViewPager.setCurrentItem(2, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewPagerAdapter.getItem(mViewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Tracker t = App.getInstance().getTracker();
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("/FirstStart/Step"+(1+mViewPager.getCurrentItem()));

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() > 0)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1, true);
        else
            super.finish();
    }

    @Override
    public void onSignedIn(GoogleApiClient client, String accountName) {

        mGoogleApiClient = client;

        Timber.d("Signed in.");
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(true);

        mPreferences.edit()
                .putBoolean(Const.SETTINGS_SIGNED_IN, true)
                .apply();

        if(mViewPager.getCurrentItem() >= 1)
            mViewPager.setCurrentItem(2, true);
    }

    @Override
    public void onSkippedSignIn() {
        mPreferences.edit()
                .putBoolean(Const.SETTINGS_SIGNED_IN, false)
                .apply();
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(false);

        mViewPager.setCurrentItem(2, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void finish() {

        requestBackup();

        Intent resultData = new Intent(FirstStartActivity.this, MainActivity.class);
        resultData.setAction("finish_first_start");
        resultData.putExtra(Const.EXTRA_GROUP_ID, mSelectedChapter);
        resultData.putExtra("selected_chapter", mSelectedChapter);
        startActivity(resultData);

        super.finish();
    }

    public void requestBackup() {
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }

    @Override
    public void onComplete(final boolean enableAnalytics, final boolean enableGcm) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                if(enableGcm && mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setLoadingScreen(true);
                        }
                    });
                    try {
                        String token = GoogleAuthUtil.getToken(FirstStartActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient),
                                "audience:server:client_id:"+getString(R.string.hub_client_id));
                        final String regid = mGcm.register(getString(R.string.gcm_sender_id));

                        GdgX client = new GdgX(token);

                        ApiRequest req = client.registerGcm(regid, new Response.Listener<GcmRegistrationResponse>() {
                            @Override
                            public void onResponse(GcmRegistrationResponse messageResponse) {
                                setLoadingScreen(false);
                                mPreferences.edit()
                                        .putBoolean(Const.SETTINGS_GCM, enableGcm)
                                        .putBoolean(Const.SETTINGS_ANALYTICS, enableAnalytics)
                                        .putString(Const.SETTINGS_GCM_REG_ID, regid)
                                        .putBoolean(Const.SETTINGS_FIRST_START, false)
                                        .putString(Const.SETTINGS_GCM_NOTIFICATION_KEY, messageResponse.getNotificationKey())
                                        .apply();

                                finish();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                setLoadingScreen(false);
                                Crouton.showText(FirstStartActivity.this, getString(R.string.server_error), Style.ALERT);
                                Timber.e("Fail", volleyError);
                            }
                        });
                        req.execute();

                        client.setHomeGdg(mPreferences.getString(Const.SETTINGS_HOME_GDG, ""), null ,null).execute();

                    } catch (IOException e) {
                        Timber.e("Token fail.", e);
                    } catch (GoogleAuthException e) {
                        Timber.e("Auth fail.", e);
                    }
                } else {
                    mPreferences.edit()
                            .putBoolean("gcm", enableGcm)
                            .putBoolean("analytics", enableAnalytics)
                            .putBoolean(Const.SETTINGS_FIRST_START, false)
                            .apply();

                    finish();
                }

                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        }.execute(null, null, null);
    }

    public class FirstStartPageAdapter extends FragmentStatePagerAdapter {
        private Context mContext;
        private Fragment[] mFragments;

        public FirstStartPageAdapter(Context ctx, FragmentManager fm) {
            super(fm);
            mContext = ctx;
            mFragments = new Fragment[] {
                FirstStartStep1Fragment.newInstance(), FirstStartStep2Fragment.newInstance(), FirstStartStep3Fragment.newInstance()
            };
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
