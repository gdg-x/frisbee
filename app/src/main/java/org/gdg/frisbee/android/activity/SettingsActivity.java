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
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockPreferenceActivity;
import com.google.android.gms.plus.GooglePlusUtil;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.PlayServicesHelper;

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

        mPreferences = mPreferenceManager.getSharedPreferences();

        initPlayServices();

        addPreferencesFromResource(R.xml.settings);

        initPreferences();
    }

    private void initPreferences() {
        final ListPreference prefHomeGdgList = (ListPreference)findPreference("gdg_home");
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
        mPreferences.edit().putBoolean(Const.SETTINGS_SIGNED_IN, false).commit();
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
