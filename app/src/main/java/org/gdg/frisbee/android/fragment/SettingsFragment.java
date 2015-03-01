/*
 * Copyright 2013-2015 The GDG Frisbee Project
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

package org.gdg.frisbee.android.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GdgX;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.widget.UpcomingEventWidgetProvider;

import java.io.IOException;

import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment {

    private GdgX mXClient;
    private GoogleCloudMessaging mGcm;

    private GoogleApiClient mGoogleApiClient;

    private Preference.OnPreferenceChangeListener mOnHomeGdgPreferenceChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            final String homeGdg = (String) o;

            if (mGoogleApiClient.isConnected() && PrefUtils.isGcmEnabled(getActivity())) {
                setHomeGdg(homeGdg);
            }
            // Update widgets to show newest chosen GdgHome events
            // TODO: Make it into class which broadcasts update need to all interested entities like MainActivity and Widgets
            App.getInstance().startService(new Intent(App.getInstance(), UpcomingEventWidgetProvider.UpdateService.class));

            return true;
        }
    };

    private Preference.OnPreferenceChangeListener mOnGcmPreferenceChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            final boolean enableGcm = (Boolean) o;

            if (mGoogleApiClient.isConnected()) {
                mLoading.setVisibility(View.VISIBLE);
                mLoading.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in));

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        try {
                            String token = GoogleAuthUtil.getToken(getActivity(), Plus.AccountApi.getAccountName(mGoogleApiClient), "oauth2: " + Scopes.PLUS_LOGIN);
                            mXClient.setToken(token);

                            if (!enableGcm) {
                                ApiRequest req = mXClient.unregisterGcm(PrefUtils.getRegistrationId(getActivity()), new Response.Listener<GcmRegistrationResponse>() {
                                            @Override
                                            public void onResponse(GcmRegistrationResponse messageResponse) {
                                                PrefUtils.setGcmSettings(getActivity(), false, null, null);
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError volleyError) {
                                                Timber.e("Fail", volleyError);
                                            }
                                        }
                                );
                                req.execute();
                            } else {
                                final String regId = mGcm.register(BuildConfig.GCM_SENDER_ID);
                                ApiRequest req = mXClient.registerGcm(regId, new Response.Listener<GcmRegistrationResponse>() {
                                            @Override
                                            public void onResponse(GcmRegistrationResponse messageResponse) {
                                                PrefUtils.setGcmSettings(getActivity(), true, regId, messageResponse.getNotificationKey());
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError volleyError) {
                                                Timber.e("Fail", volleyError);
                                            }
                                        }
                                );
                                req.execute();

                                setHomeGdg(PrefUtils.getHomeChapterIdNotNull(getActivity()));
                            }
                        } catch (IOException e) {
                            Timber.e("(Un)Register GCM gailed (IO)", e);
                            e.printStackTrace();
                        } catch (GoogleAuthException e) {
                            Timber.e("(Un)Register GCM gailed (Auth)", e);
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void o) {
                        super.onPostExecute(o);

                        Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
                        fadeOut.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                mLoading.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                        mLoading.startAnimation(fadeOut);
                    }
                }.execute();
            }
            return true;
        }
    };

    private Preference.OnPreferenceChangeListener mOnAnalyticsPreferenceChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            boolean analytics = (Boolean) o;
            GoogleAnalytics.getInstance(getActivity()).setAppOptOut(!analytics);
            return true;
        }
    };
    private LinearLayout mLoading;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGoogleApiClient = ((GdgActivity) getActivity()).getGoogleApiClient();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mXClient = new GdgX();
        mGcm = GoogleCloudMessaging.getInstance(getActivity());

        getPreferenceManager().setSharedPreferencesName(PrefUtils.PREF_NAME);
        addPreferencesFromResource(R.xml.settings);
        initPreferences();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoading = new LinearLayout(getActivity());
    }

    private void initPreferences() {
        final ListPreference prefHomeGdgList = (ListPreference) findPreference(PrefUtils.SETTINGS_HOME_GDG);
        if (prefHomeGdgList != null) {
            App.getInstance().getModelCache().getAsync("chapter_list_hub", false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    Directory directory = (Directory) item;

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

            prefHomeGdgList.setOnPreferenceChangeListener(mOnHomeGdgPreferenceChange);
        }

        CheckBoxPreference prefGcm = (CheckBoxPreference) findPreference(PrefUtils.SETTINGS_GCM);
        if (prefGcm != null) {
            prefGcm.setOnPreferenceChangeListener(mOnGcmPreferenceChange);
        }

        CheckBoxPreference prefGoogleSignIn = (CheckBoxPreference) findPreference(PrefUtils.SETTINGS_SIGNED_IN);
        if (prefGoogleSignIn != null) {
            prefGoogleSignIn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    boolean signedIn = (Boolean) o;

                    if (!signedIn) {
                        if (mGoogleApiClient.isConnected()) {
                            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                            mGoogleApiClient.disconnect();
                            mGoogleApiClient.connect();
                        }
                    } else {
                        if (!mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.connect();
                        }
                    }
                    // TODO: Re-implement logout....

                    return true;
                }
            });
        }

        CheckBoxPreference prefAnalytics = (CheckBoxPreference) findPreference(PrefUtils.SETTINGS_ANALYTICS);
        if (prefAnalytics != null) {
            prefAnalytics.setOnPreferenceChangeListener(mOnAnalyticsPreferenceChange);
        }
    }

    private void setHomeGdg(final String homeGdg) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                try {
                    GdgActivity activity = (GdgActivity) getActivity();
                    String token = GoogleAuthUtil.getToken(
                            activity,
                            Plus.AccountApi.getAccountName(activity.getGoogleApiClient()),
                            "oauth2: " + Scopes.PLUS_LOGIN);
                    mXClient.setToken(token);
                    mXClient.setHomeGdg(homeGdg, null, null).execute();
                } catch (IOException | GoogleAuthException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    // TODO: Re-Implement with GMS 4.3
    public void onSignInFailed() {
        Timber.d("onSignInFailed");
        PrefUtils.setLoggedOut(getActivity());
        CheckBoxPreference prefGoogleSignIn = (CheckBoxPreference) findPreference("gdg_signed_in");
        if (prefGoogleSignIn != null) {
            prefGoogleSignIn.setChecked(false);
        }
    }

}
