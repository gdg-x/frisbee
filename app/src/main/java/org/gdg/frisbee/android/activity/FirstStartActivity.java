/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.activity;

import android.app.backup.BackupManager;
import android.content.Intent;
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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.GcmRegistrationRequest;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.api.model.HomeGdgRequest;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.fragment.FirstStartStep1Fragment;
import org.gdg.frisbee.android.fragment.FirstStartStep2Fragment;
import org.gdg.frisbee.android.fragment.FirstStartStep3Fragment;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.NonSwipeableViewPager;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class FirstStartActivity extends ActionBarActivity implements 
        FirstStartStep1Fragment.Step1Listener, 
        FirstStartStep2Fragment.Step2Listener, 
        FirstStartStep3Fragment.Step3Listener {

    public static final final String ACTION_FIRST_START = "finish_first_start";

    private GoogleCloudMessaging mGcm;

    @InjectView(R.id.pager)
    NonSwipeableViewPager mViewPager;

    @InjectView(R.id.loading)
    LinearLayout mLoading;

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

        ButterKnife.inject(this);

        mViewPagerAdapter = new FirstStartPageAdapter(getSupportFragmentManager());
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
                t.setScreenName("/FirstStart/Step" + (1 + i));

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
        if (show) {
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
        PrefUtils.setHomeChapter(this, chapter);

        if (mGoogleApiClient == null) {
            mViewPager.setCurrentItem(1, true);
        } else {
            mViewPager.setCurrentItem(2, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mViewPagerAdapter.getItem(mViewPager.getCurrentItem()).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Tracker t = App.getInstance().getTracker();
        // Set screen name.
        // Where path is a String representing the screen name.
        t.setScreenName("/FirstStart/Step" + (1 + mViewPager.getCurrentItem()));

        // Send a screen view.
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() > 0) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        } else {
            super.finish();
        }
    }

    @Override
    public void onSignedIn(GoogleApiClient client, String accountName) {

        mGoogleApiClient = client;

        Timber.d("Signed in.");
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(true);

        PrefUtils.setSignedIn(this);

        if (mViewPager.getCurrentItem() >= 1) {
            mViewPager.setCurrentItem(2, true);
        }
    }

    @Override
    public void onSkippedSignIn() {
        PrefUtils.setLoggedOut(this);
        FirstStartStep3Fragment step3 = (FirstStartStep3Fragment) mViewPagerAdapter.getItem(2);
        step3.setSignedIn(false);

        mViewPager.setCurrentItem(2, true);
    }

    @Override
    public void finish() {

        requestBackup();

        Intent resultData = new Intent(this, MainActivity.class);
        resultData.setAction(ACTION_FIRST_START);
        resultData.putExtra(Const.EXTRA_CHAPTER_ID, mSelectedChapter.getGplusId());
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

                if (enableGcm && PrefUtils.isSignedIn(FirstStartActivity.this)) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setLoadingScreen(true);
                        }
                    });
                    try {
                        String token = GoogleAuthUtil.getToken(FirstStartActivity.this,
                                Plus.AccountApi.getAccountName(mGoogleApiClient),
                                "audience:server:client_id:" + BuildConfig.HUB_CLIENT_ID);
                        final String regid = mGcm.register(BuildConfig.GCM_SENDER_ID);


                        final GdgXHub client = App.getInstance().getGdgXHub();
                        client.registerGcm("Bearer " + token, new GcmRegistrationRequest(regid), new Callback<GcmRegistrationResponse>() {
                            @Override
                            public void success(GcmRegistrationResponse gcmRegistrationResponse, retrofit.client.Response response) {
                                setLoadingScreen(false);
                                PrefUtils.setInitialSettings(
                                        FirstStartActivity.this,
                                        /* enableGcm */ true,
                                        enableAnalytics,
                                        regid,
                                        gcmRegistrationResponse.getNotificationKey());
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                setLoadingScreen(false);
                                Crouton.showText(FirstStartActivity.this, getString(R.string.server_error), Style.ALERT);
                                Timber.e(error, "Fail");
                            }
                        });

                        client.setHomeGdg("Bearer " + token,
                                new HomeGdgRequest(PrefUtils.getHomeChapterIdNotNull(FirstStartActivity.this)),
                                new Callback<Void>() {
                                    @Override
                                    public void success(Void aVoid, Response response) {
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        Timber.e(error, "Setting Home failed.");
                                    }
                                });

                    } catch (IOException e) {
                        Timber.e("Token fail. %s", e);
                    } catch (GoogleAuthException e) {
                        Timber.e("Auth fail. %s", e);
                    }
                } else {
                    PrefUtils.setInitialSettings(FirstStartActivity.this, enableGcm, enableAnalytics, null, null);
                    finish();
                }

                return null;
            }
        }.execute();
    }

    public class FirstStartPageAdapter extends FragmentStatePagerAdapter {
        private Fragment[] mFragments;

        public FirstStartPageAdapter(FragmentManager fm) {
            super(fm);
            mFragments = new Fragment[] {
                new FirstStartStep1Fragment(),
                new FirstStartStep2Fragment(),
                new FirstStartStep3Fragment()
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
