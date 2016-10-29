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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.SettingsActivity;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.app.GoogleApiClientFactory;
import org.gdg.frisbee.android.chapter.ChapterSelectDialog;
import org.gdg.frisbee.android.utils.PrefUtils;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final int RC_SIGN_IN = 101;

    private GoogleApiClient signInClient;

    private Preference.OnPreferenceChangeListener mOnAnalyticsPreferenceChange =
        new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean analytics = (Boolean) newValue;
                GoogleAnalytics.getInstance(getActivity()).setAppOptOut(!analytics);
                return true;
            }
        };
    private SharedPreferences sharedPreferences;
    private CheckBoxPreference prefGoogleSignIn;
    @Nullable private Chapter homeChapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        signInClient = GoogleApiClientFactory.createForSignIn((FragmentActivity) getActivity(), null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeChapter = PrefUtils.getHomeChapter(getActivity());

        sharedPreferences = PrefUtils.prefs(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        getPreferenceManager().setSharedPreferencesName(PrefUtils.PREF_NAME);
        addPreferencesFromResource(R.xml.settings);
        initPreferences();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simple_prefs, container, false);
    }

    private void initPreferences() {
        initGdgHomePreference();

        initGoogleSignInPreference();

        initAnalyticsPreference();
    }

    private void initGdgHomePreference() {
        Preference preference = findPreference(PrefUtils.SETTINGS_HOME_GDG);
        if (homeChapter != null) {
            preference.setSummary(homeChapter.getName());
        }
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SettingsActivity activity = (SettingsActivity) getActivity();
                Chapter homeChapter = PrefUtils.getHomeChapter(getActivity());
                ChapterSelectDialog.newInstance(homeChapter)
                    .show(activity.getSupportFragmentManager(), null);
                return true;
            }
        });
    }

    private void initGoogleSignInPreference() {
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
    }

    private void initAnalyticsPreference() {
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PrefUtils.SETTINGS_HOME_GDG.equals(key)) {
            homeChapter = PrefUtils.getHomeChapter(getActivity());
            Preference preference = findPreference(key);
            if (homeChapter != null) {
                preference.setSummary(homeChapter.getName());
            }
        }
    }
}
