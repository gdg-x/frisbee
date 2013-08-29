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
import android.util.Log;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
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
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import org.gdg.frisbee.android.view.NonSwipeableViewPager;

import java.io.IOException;

import roboguice.inject.InjectView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 29.04.13
 * Time: 14:48
 */
public class FirstStartActivity extends RoboSherlockFragmentActivity implements FirstStartStep1Fragment.Step1Listener, FirstStartStep2Fragment.Step2Listener, FirstStartStep3Fragment.Step3Listener, PlayServicesHelper.PlayServicesHelperListener {

    private static String LOG_TAG = "GDG-FirstStartActivity";

    private PlayServicesHelper mPlayHelper = null;
    private GoogleCloudMessaging mGcm;
    protected int mRequestedClients = PlayServicesHelper.CLIENT_GAMES | PlayServicesHelper.CLIENT_PLUS;

    @InjectView(R.id.pager)
    private NonSwipeableViewPager mViewPager;

    @InjectView(R.id.loading)
    private LinearLayout mLoading;

    private SharedPreferences mPreferences;
    private Chapter mSelectedChapter;
    private FirstStartPageAdapter mViewPagerAdapter;

    private Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_first_start);

        App.getInstance().updateLastLocation();
        mPreferences = getSharedPreferences("gdg", MODE_PRIVATE);

        mViewPagerAdapter = new FirstStartPageAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mGcm = GoogleCloudMessaging.getInstance(this);

        mPlayHelper = new PlayServicesHelper(this);
        mPlayHelper.setup(this, mRequestedClients);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onPageSelected(int i) {
                App.getInstance().getTracker().sendView("/FirstStart/Step"+(1+i));
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
    }

    public PlayServicesHelper getPlayServicesHelper() {
        return mPlayHelper;
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
        mViewPager.setCurrentItem(1, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOG_TAG, "onActivityResult");

        mPlayHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mPlayHelper.onStart(this);
        App.getInstance().getTracker().sendView("/FirstStart/Step"+(1+mViewPager.getCurrentItem()));
    }

    @Override
    public void onBackPressed() {
        if(mViewPager.getCurrentItem() > 0)
            mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1, true);
        else
            super.finish();
    }

    @Override
    public void onSignedIn(String accountName) {
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
        mPlayHelper.onStop();
    }

    @Override
    public void finish() {

        requestBackup();

        Intent resultData = new Intent(FirstStartActivity.this, MainActivity.class);
        resultData.setAction("finish_first_start");
        resultData.putExtra("selected_chapter", mSelectedChapter);
        startActivity(resultData);
        super.finish();
    }

    public void requestBackup() {
        BackupManager bm = new BackupManager(this);
        bm.dataChanged();
    }

    @Override
    public void onSignInFailed() {
        Log.e(LOG_TAG, "SignIn failed");
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(LOG_TAG, "Signed in.");
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(true);

        onSignedIn(mPlayHelper.getPlusClient().getAccountName());
    }

    @Override
    public void onComplete(final boolean enableAnalytics, final boolean enableGcm) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                if(enableGcm && mPreferences.getBoolean(Const.SETTINGS_SIGNED_IN, false)) {
                    setLoadingScreen(true);
                    try {
                        String token = GoogleAuthUtil.getToken(FirstStartActivity.this, mPlayHelper.getPlusClient().getAccountName(), "oauth2: "+ Scopes.PLUS_LOGIN);
                        final String regid = mGcm.register(getString(R.string.gcm_sender_id));

                        GdgX client = new GdgX(token);

                        ApiRequest req = client.registerGcm(regid, new Response.Listener<GcmRegistrationResponse>() {
                            @Override
                            public void onResponse(GcmRegistrationResponse messageResponse) {
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
                                Log.e(LOG_TAG, "Fail", volleyError);
                            }
                        });
                        req.execute();

                        client.setHomeGdg(mPreferences.getString(Const.SETTINGS_HOME_GDG, ""), null ,null).execute();

                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Token fail.", e);
                    } catch (GoogleAuthException e) {
                        Log.e(LOG_TAG, "Auth fail.", e);
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
