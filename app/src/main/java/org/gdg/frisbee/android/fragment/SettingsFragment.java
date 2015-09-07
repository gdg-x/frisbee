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

package org.gdg.frisbee.android.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.GcmRegistrationRequest;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.api.model.HomeGdgRequest;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.appwidget.UpcomingEventWidgetProvider;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;

import java.io.IOException;

import retrofit.Callback;
import retrofit.Response;
import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment {

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
                            if (!mGoogleApiClient.isConnected()) {
                                mGoogleApiClient.blockingConnect();
                            }

                            GdgXHub client = App.getInstance().getGdgXHub();
                            String token = GoogleAuthUtil.getToken(getActivity(),
                                    Plus.AccountApi.getAccountName(mGoogleApiClient),
                                    "oauth2: " + Scopes.PLUS_LOGIN);

                            if (!enableGcm) {
                                client.unregisterGcm("Bearer " + token, new GcmRegistrationRequest(PrefUtils.getRegistrationId(getActivity())))
                                        .enqueue(new Callback<GcmRegistrationResponse>() {
                                            @Override
                                            public void onResponse(Response<GcmRegistrationResponse> response) {
                                                PrefUtils.setGcmSettings(getActivity(), false, null, null);
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
                                                Timber.e(t, "Fail");
                                            }
                                        });
                            } else {
                                final String regId = mGcm.register(BuildConfig.GCM_SENDER_ID);

                                client.registerGcm("Bearer " + token, new GcmRegistrationRequest(regId))
                                        .enqueue(new Callback<GcmRegistrationResponse>() {
                                            @Override
                                            public void onResponse(Response<GcmRegistrationResponse> response) {
                                                PrefUtils.setGcmSettings(getActivity(),
                                                        true,
                                                        regId,
                                                        response.body().getNotificationKey());
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
                                                Timber.e(t, "Fail");
                                            }
                                        });

                                setHomeGdg(PrefUtils.getHomeChapterIdNotNull(getActivity()));
                            }
                        } catch (IOException | GoogleAuthException e) {
                            Timber.e(e, "(Un)Register GCM failed");
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

        mGcm = GoogleCloudMessaging.getInstance(getActivity());

        getPreferenceManager().setSharedPreferencesName(PrefUtils.PREF_NAME);
        addPreferencesFromResource(R.xml.settings);
        initPreferences();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_prefs, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoading = new LinearLayout(getActivity());
    }

    private void initPreferences() {
        final ListPreference prefHomeGdgList = (ListPreference) findPreference(PrefUtils.SETTINGS_HOME_GDG);
        if (prefHomeGdgList != null) {
            prefHomeGdgList.setEnabled(false);

            App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    Directory directory = (Directory) item;

                    CharSequence[] entries = new String[directory.getGroups().size()];
                    CharSequence[] entryValues = new String[directory.getGroups().size()];

                    int i = 0;
                    for (Chapter chapter : directory.getGroups()) {
                        entries[i] = chapter.getName();
                        entryValues[i] = chapter.getGplusId();
                        i++;
                    }
                    prefHomeGdgList.setEntries(entries);
                    prefHomeGdgList.setEntryValues(entryValues);
                    prefHomeGdgList.setEnabled(true);
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
                            PrefUtils.setLoggedOut(getActivity());
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

                    App.getInstance().getGdgXHub().setHomeGdg("Bearer " + token,
                            new HomeGdgRequest(homeGdg))
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Response<Void> response) {

                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    Timber.e(t, "Setting Home failed.");
                                }
                            });
                } catch (IOException | GoogleAuthException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }
}
