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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.SettingsActivity;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.HomeGdgRequest;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.appwidget.UpcomingEventWidgetProvider;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.LocationListPreference;

import java.io.IOException;

public class SettingsFragment extends PreferenceFragment {

    private GoogleApiClient mGoogleApiClient;
    private LinearLayout mLoading;

    private Preference.OnPreferenceChangeListener mOnHomeGdgPreferenceChange =
        new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                final String homeGdg = (String) o;

                if (mGoogleApiClient.isConnected()) {
                    setHomeGdg(homeGdg);
                }
                // Update widgets to show newest chosen GdgHome events
                App.getInstance().startService(new Intent(App.getInstance(),
                    UpcomingEventWidgetProvider.UpdateService.class));

                return true;
            }
        };

    private Preference.OnPreferenceChangeListener mOnAnalyticsPreferenceChange =
        new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean analytics = (Boolean) newValue;
                GoogleAnalytics.getInstance(getActivity()).setAppOptOut(!analytics);
                return true;
            }
        };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGoogleApiClient = ((GdgActivity) getActivity()).getGoogleApiClient();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        final LocationListPreference prefHomeGdgList =
            (LocationListPreference) findPreference(PrefUtils.SETTINGS_HOME_GDG);
        if (prefHomeGdgList != null) {
            prefHomeGdgList.setEnabled(false);

            App.getInstance().getModelCache().getAsync(ModelCache.KEY_CHAPTER_LIST_HUB, false,
                new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        Directory directory = (Directory) item;

                        String[] entries = new String[directory.getGroups().size()];
                        String[] entryValues = new String[directory.getGroups().size()];

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

        CheckBoxPreference prefGoogleSignIn = (CheckBoxPreference) findPreference(PrefUtils.SETTINGS_SIGNED_IN);
        if (prefGoogleSignIn != null) {
            if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity())
                == ConnectionResult.SUCCESS) {
                prefGoogleSignIn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        boolean signedIn = (Boolean) newValue;
                        if (signedIn) {
                            if (mGoogleApiClient.isConnected()) {
                                disconnectGoogleApiClient();
                            }
                            PrefUtils.setSignedIn(getActivity());
                            createConnectedGoogleApiClient();
                        } else {
                            if (mGoogleApiClient.isConnected()) {
                                Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                                disconnectGoogleApiClient();
                            }
                            PrefUtils.setLoggedOut(getActivity());
                            createConnectedGoogleApiClient();
                        }
                        return true;
                    }
                });
            } else {
                PreferenceScreen root = (PreferenceScreen) findPreference(PrefUtils.SETTINGS_ROOT);
                root.removePreference(prefGoogleSignIn);
            }
        }

        CheckBoxPreference prefAnalytics = (CheckBoxPreference) findPreference(PrefUtils.SETTINGS_ANALYTICS);
        if (prefAnalytics != null) {
            prefAnalytics.setOnPreferenceChangeListener(mOnAnalyticsPreferenceChange);
        }
    }

    private void createConnectedGoogleApiClient() {
        mGoogleApiClient = GoogleApiClientFactory.createWith(getActivity());
        mGoogleApiClient.registerConnectionCallbacks((SettingsActivity) getActivity());
        mGoogleApiClient.registerConnectionFailedListener((SettingsActivity) getActivity());
        mGoogleApiClient.connect();
    }

    private void disconnectGoogleApiClient() {
        mGoogleApiClient.unregisterConnectionCallbacks((SettingsActivity) getActivity());
        mGoogleApiClient.unregisterConnectionFailedListener((SettingsActivity) getActivity());
        mGoogleApiClient.disconnect();
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
                        .execute();
                } catch (IOException | GoogleAuthException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }
}
