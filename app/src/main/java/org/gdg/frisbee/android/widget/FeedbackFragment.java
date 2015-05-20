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

package org.gdg.frisbee.android.widget;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.doorbell.android.DoorbellApi;
import io.doorbell.android.manavo.rest.RestCallback;
import timber.log.Timber;

public class FeedbackFragment extends DialogFragment {
    private static final String PROPERTY_MODEL = "Model";
    private static final String PROPERTY_ANDROID_VERSION = "Android Version";
    private static final String PROPERTY_WI_FI_ENABLED = "WiFi enabled";
    private static final String PROPERTY_MOBILE_DATA_ENABLED = "Mobile Data enabled";
    private static final String PROPERTY_GPS_ENABLED = "GPS enabled";
    private static final String PROPERTY_SCREEN_RESOLUTION = "Screen Resolution";
    private static final String PROPERTY_ACTIVITY = "Activity";
    private static final String PROPERTY_APP_VERSION_NAME = "App Version Name";
    private static final String PROPERTY_APP_VERSION_CODE = "App Version Code";
//    private static final String POWERED_BY_DOORBELL_TEXT = "Powered by <a href=\"https://doorbell.io\">Doorbell.io</a>";

    @InjectView(R.id.feedback_message_text) EditText mMessageField;
    @InjectView(R.id.feedback_email_text) AutoCompleteTextView mEmailField;

    private JSONObject mProperties;
    private DoorbellApi mApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApi = new DoorbellApi(getActivity());
        mProperties = new JSONObject();
        mApi.setAppId(BuildConfig.DOORBELL_ID);
        mApi.setApiKey(BuildConfig.DOORBELL_APP_KEY);

        addProperty("loggedIn", PrefUtils.isSignedIn(getActivity())); // Optionally add some properties
        addProperty("appStarts", PrefUtils.getAppStarts(getActivity()));
        buildProperties();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View feedbackView = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_feedback, (ViewGroup) getView(), false);
        ButterKnife.inject(this, feedbackView);
        setupEmailAutocomplete();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(feedbackView)
                .setTitle(R.string.feedback)
                .setCancelable(true)
                .setNegativeButton(R.string.feedback_cancel, null)
                .setPositiveButton(R.string.feedback_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendFeedback();
                    }
                });

        return builder.create();
    }

    private void sendFeedback() {
        mApi.setLoadingMessage(getActivity().getString(R.string.feedback_sending));
        mApi.setCallback(new RestCallback() {
            public void success(Object obj) {
                //TODO add feedback
//                                Toast.makeText(getActivity(), obj.toString(), Toast.LENGTH_SHORT).show();
                mMessageField.setText("");
                mProperties = new JSONObject();

                Activity activity = getActivity();
                if (activity != null && activity instanceof GdgActivity) {
                    ((GdgActivity) activity).getAchievementActionHandler()
                            .handleKissesFromGdgXTeam();
                }
            }
        });
        mApi.sendFeedback(mMessageField.getText().toString(),
                mEmailField.getText().toString(), mProperties, "");
    }

    private void setupEmailAutocomplete() {
        //Set email AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_item);
        Set<String> accountsSet = new HashSet<>();
        Account[] deviceAccounts = AccountManager.get(getActivity()).getAccounts();
        for (Account account : deviceAccounts) {
            if (Utils.isEmailAddress(account.name)) {
                accountsSet.add(account.name);
            }
        }
        adapter.addAll(accountsSet);
        mEmailField.setAdapter(adapter);
    }

    private void buildProperties() {
        addProperty(PROPERTY_MODEL, Build.MODEL);
        addProperty(PROPERTY_ANDROID_VERSION, VERSION.RELEASE);

        try {
            WifiManager cm = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            WifiInfo e = cm.getConnectionInfo();
            SupplicantState mobileDataEnabled = e.getSupplicantState();
            addProperty(PROPERTY_WI_FI_ENABLED, mobileDataEnabled);
        } catch (Exception e) {
            Timber.d(e, "Wifi Manager problem.");
        }

        boolean mobileDataEnabled1 = false;
        ConnectivityManager cm1 = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        try {
            Class e1 = Class.forName(cm1.getClass().getName());
            Method resolution = e1.getDeclaredMethod("getMobileDataEnabled", new Class[0]);
            resolution.setAccessible(true);
            mobileDataEnabled1 = (Boolean) resolution.invoke(cm1, new Object[0]);
        } catch (Exception e) {
            Timber.d(e, "Mobil data problem.");
        }

        addProperty(PROPERTY_MOBILE_DATA_ENABLED, mobileDataEnabled1);

        try {
            LocationManager e2 = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            boolean resolution1 = e2.isProviderEnabled("gps");
            addProperty(PROPERTY_GPS_ENABLED, resolution1);
        } catch (Exception e) {
            Timber.d(e, "GPS problem.");
        }

        try {
            DisplayMetrics e3 = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(e3);
            String resolution2 = Integer.toString(e3.widthPixels) + "x" + Integer.toString(e3.heightPixels);
            addProperty(PROPERTY_SCREEN_RESOLUTION, resolution2);
        } catch (Exception e) {
            Timber.d(e, "Screen density problem.");
        }

        try {
            String e4 = getActivity().getClass().getSimpleName();
            addProperty(PROPERTY_ACTIVITY, e4);
        } catch (Exception e) {
            Timber.d(e, "Activity name problem.");
        }

        PackageManager manager = getActivity().getPackageManager();
        try {
            PackageInfo e = manager.getPackageInfo(getActivity().getPackageName(), 0);
            addProperty(PROPERTY_APP_VERSION_NAME, e.versionName);
            addProperty(PROPERTY_APP_VERSION_CODE, e.versionCode);
        } catch (PackageManager.NameNotFoundException var7) {
            Timber.d("Problem with PackageManager");
        }
    }

    public void addProperty(String key, Object value) {
        try {
            mProperties.put(key, value);
        } catch (JSONException e) {
            Timber.d(e, "JSON exception.");
        }
    }


}
