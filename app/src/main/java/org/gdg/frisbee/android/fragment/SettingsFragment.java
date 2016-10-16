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
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.HomeGdgRequest;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.appwidget.UpcomingEventWidgetProvider;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.PlusUtils;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.LocationListPreference;

import java.io.IOException;

public class SettingsFragment extends PreferenceFragment {
    private static final int RC_SIGN_IN = 101;

    private GoogleApiClient signInClient;

    private Preference.OnPreferenceChangeListener mOnHomeGdgPreferenceChange =
        new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                final String homeGdg = (String) o;

                setHomeGdg(homeGdg);
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
    private CheckBoxPreference prefGoogleSignIn;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        signInClient = GoogleApiClientFactory.createForSignIn((FragmentActivity) getActivity(), null);
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

        prefGoogleSignIn = (CheckBoxPreference) findPreference(PrefUtils.SETTINGS_SIGNED_IN);
        if (isGooglePlayServicesAvailable()) {
            prefGoogleSignIn.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean signedIn = (Boolean) newValue;
                    if (signedIn) {
                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(signInClient);
                        startActivityForResult(signInIntent, RC_SIGN_IN);
                    } else {
                        Auth.GoogleSignInApi.signOut(signInClient);
                        PrefUtils.setSignedOut(getActivity());
                    }
                    return true;
                }
            });
        } else {
            prefGoogleSignIn.setEnabled(false);
        }

        CheckBoxPreference prefAnalytics = (CheckBoxPreference) findPreference(PrefUtils.SETTINGS_ANALYTICS);
        if (prefAnalytics != null) {
            prefAnalytics.setOnPreferenceChangeListener(mOnAnalyticsPreferenceChange);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                PrefUtils.setSignedIn(getActivity());
            } else {
                prefGoogleSignIn.setChecked(false);
            }
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity())
            == ConnectionResult.SUCCESS;
    }

    private void setHomeGdg(final String homeGdg) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                GoogleSignInAccount account = PlusUtils.getCurrentAccount(getActivity());
                if (account == null) {
                    return null;
                }
                try {
                    App.getInstance().getGdgXHub().setHomeGdg("Bearer " + account.getIdToken(),
                        new HomeGdgRequest(homeGdg))
                        .execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }
}
