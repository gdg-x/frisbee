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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockPreferenceActivity;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.GooglePlusUtil;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GdgX;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.PlayServicesHelper;
import org.gdg.frisbee.android.widget.UpcomingEventWidgetProvider;

import java.io.IOException;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 25.06.13
 * Time: 21:15
 */
public class SettingsActivity extends RoboSherlockPreferenceActivity implements PlayServicesHelper.PlayServicesHelperListener {

    private static final String LOG_TAG = "GDG-SettingsActivity";
    private SharedPreferences mPreferences;
    private PreferenceManager mPreferenceManager;
    private PlayServicesHelper mPlayServicesHelper;
    private GdgX mXClient;
    private GoogleCloudMessaging mGcm;

    private LinearLayout mLoading;

    protected void initPlayServices() {
        int errorCode = GooglePlusUtil.checkGooglePlusApp(this);
        if (errorCode != GooglePlusUtil.SUCCESS) {
            GooglePlusUtil.getErrorDialog(errorCode, this, 0).show();
        } else {
            mPlayServicesHelper = new PlayServicesHelper(this);
            mPlayServicesHelper.setup(this, PlayServicesHelper.CLIENT_PLUS | PlayServicesHelper.CLIENT_GAMES);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mPlayServicesHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mPlayServicesHelper.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mPreferenceManager = getPreferenceManager();
        mPreferenceManager.setSharedPreferencesName("gdg");

        mXClient = new GdgX();
        mGcm = GoogleCloudMessaging.getInstance(this);

        setContentView(R.layout.activity_settings);

        App.getInstance().getTracker().sendView("/Settings");

        mLoading = (LinearLayout) findViewById(R.id.loading);

        mPreferences = mPreferenceManager.getSharedPreferences();

        initPlayServices();

        addPreferencesFromResource(R.xml.settings);

        initPreferences();
    }

    private void initPreferences() {
        final ListPreference prefHomeGdgList = (ListPreference)findPreference(Const.SETTINGS_HOME_GDG);
        if(prefHomeGdgList != null) {
            App.getInstance().getModelCache().getAsync("chapter_list", false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    Directory directory = (Directory)item;

                    CharSequence entries[] = new String[directory.getGroups().size()];
                    CharSequence entryValues[] = new String[directory.getGroups().size()];

                    int i = 0;
                    for (Chapter chapter : directory.getGroups()) {
                        entries[i] = chapter.getName();
                        entryValues[i] = chapter.getGplusId();
                        i++;
                    }
                    prefHomeGdgList.setEntries(entries);
                    prefHomeGdgList.setEntryValues(entryValues);
                }

                @Override
                public void onNotFound(String key) {

                }
            });
            prefHomeGdgList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    final String homeGdg = (String) o;

                    if (mPlayServicesHelper.isSignedIn() &&  mPreferences.getBoolean("gcm", true)) {
                        setHomeGdg(homeGdg);
                    }
                    // Update widgets to show newest chosen GdgHome events
                    // TODO: Make it into class which broadcasts update need to all interested entities like MainActivity and Widgets
                    App.getInstance().startService(new Intent(App.getInstance(), UpcomingEventWidgetProvider.UpdateService.class));

                    return true;
                }
            });
        }

        CheckBoxPreference prefGcm = (CheckBoxPreference)findPreference(Const.SETTINGS_GCM);
        if(prefGcm != null) {
            prefGcm.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    final boolean enableGcm = (Boolean) o;

                    if (mPlayServicesHelper.isSignedIn()) {
                        mLoading.setVisibility(View.VISIBLE);
                        mLoading.startAnimation(AnimationUtils.loadAnimation(SettingsActivity.this, R.anim.fade_in));

                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... voids) {
                                    try {
                                        String token = GoogleAuthUtil.getToken(SettingsActivity.this, mPlayServicesHelper.getPlusClient().getAccountName(), "oauth2: " + Scopes.PLUS_LOGIN);
                                        mXClient.setToken(token);

                                        if(!enableGcm) {
                                            ApiRequest req = mXClient.unregisterGcm(mPreferences.getString(Const.SETTINGS_GCM_REG_ID,""), new Response.Listener<GcmRegistrationResponse>() {
                                                        @Override
                                                        public void onResponse(GcmRegistrationResponse messageResponse) {
                                                            mPreferences.edit()
                                                                    .putBoolean(Const.SETTINGS_GCM, false)
                                                                    .remove(Const.SETTINGS_GCM_REG_ID)
                                                                    .apply();
                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError volleyError) {
                                                            Log.e(LOG_TAG, "Fail", volleyError);
                                                        }
                                                    }
                                            );
                                            req.execute();
                                        } else {
                                            final String regid = mGcm.register(getString(R.string.gcm_sender_id));
                                            ApiRequest req = mXClient.registerGcm(regid, new Response.Listener<GcmRegistrationResponse>() {
                                                        @Override
                                                        public void onResponse(GcmRegistrationResponse messageResponse) {
                                                            mPreferences.edit()
                                                                    .putBoolean(Const.SETTINGS_GCM, true)
                                                                    .putString(Const.SETTINGS_GCM_REG_ID, regid)
                                                                    .putString(Const.SETTINGS_GCM_NOTIFICATION_KEY, messageResponse.getNotificationKey())
                                                                    .apply();
                                                        }
                                                    }, new Response.ErrorListener() {
                                                        @Override
                                                        public void onErrorResponse(VolleyError volleyError) {
                                                            Log.e(LOG_TAG, "Fail", volleyError);
                                                        }
                                                    }
                                            );
                                            req.execute();

                                            setHomeGdg(mPreferences.getString(Const.SETTINGS_HOME_GDG, ""));
                                        }
                                    } catch (IOException e) {
                                        Log.e(LOG_TAG, "(Un)Register GCM gailed (IO)", e);
                                        e.printStackTrace();
                                    } catch (GoogleAuthException e) {
                                        Log.e(LOG_TAG, "(Un)Register GCM gailed (Auth)", e);
                                        e.printStackTrace();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void o) {
                                    super.onPostExecute(o);

                                    Animation fadeOut = AnimationUtils.loadAnimation(SettingsActivity.this, R.anim.fade_out);
                                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                        @Override
                                        public void onAnimationStart(Animation animation) {}

                                        @Override
                                        public void onAnimationEnd(Animation animation) {
                                            mLoading.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {}
                                    });
                                    mLoading.startAnimation(fadeOut);
                                }
                            }.execute();
                    }
                    return true;
                }
            });
        }

        CheckBoxPreference prefGoogleSignIn = (CheckBoxPreference)findPreference("gdg_signed_in");
        if(prefGoogleSignIn != null) {
            prefGoogleSignIn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean signedIn = (Boolean) o;

                    if(!signedIn) {
                        if (mPlayServicesHelper.isSignedIn()) {
                            mPlayServicesHelper.signOut();
                        }
                    } else {
                        if (!mPlayServicesHelper.isSignedIn()) {
                            mPlayServicesHelper.beginUserInitiatedSignIn();
                        }
                    }

                    return true;
                }
            });
        }

        CheckBoxPreference prefAnalytics = (CheckBoxPreference)findPreference("analytics");
        if(prefAnalytics != null) {
            prefAnalytics.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean analytics = (Boolean) o;
                    GoogleAnalytics.getInstance(SettingsActivity.this).setAppOptOut(!analytics);
                    return true;
                }
            });
        }
    }

    private void setHomeGdg(final String homeGdg) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                String token = null;
                try {
                    token = GoogleAuthUtil.getToken(SettingsActivity.this, mPlayServicesHelper.getPlusClient().getAccountName(), "oauth2: " + Scopes.PLUS_LOGIN);
                    mXClient.setToken(token);

                    mXClient.setHomeGdg(homeGdg, null ,null).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
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
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

        mPlayServicesHelper.onActivityResult(requestCode,responseCode,intent);
    }

    @Override
    public void onSignInFailed() {
        Log.d(LOG_TAG, "onSignInFailed");
        mPreferences.edit().putBoolean(Const.SETTINGS_SIGNED_IN, false).apply();
        CheckBoxPreference prefGoogleSignIn = (CheckBoxPreference)findPreference("gdg_signed_in");
        if(prefGoogleSignIn != null) {
            prefGoogleSignIn.setChecked(false);
        }
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(LOG_TAG, "onSignInSucceeded");
    }
}
