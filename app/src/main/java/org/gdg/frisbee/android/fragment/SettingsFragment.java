package org.gdg.frisbee.android.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
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

import java.io.IOException;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GdgX;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.widget.UpcomingEventWidgetProvider;

import timber.log.Timber;

public class SettingsFragment extends PreferenceFragment {

    private static final String LOG_TAG = "GDG-SettingsFragment";

    private PreferenceManager mPreferenceManager;
    private GdgX mXClient;
    private GoogleCloudMessaging mGcm;
    private SharedPreferences mPreferences;

    private GoogleApiClient mGoogleApiClient;

    private Preference.OnPreferenceChangeListener mOnHomeGdgPreferenceChange = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            final String homeGdg = (String) o;

            if (mGoogleApiClient.isConnected() && mPreferences.getBoolean("gcm", true)) {
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
                                ApiRequest req = mXClient.unregisterGcm(mPreferences.getString(Const.SETTINGS_GCM_REG_ID, ""), new Response.Listener<GcmRegistrationResponse>() {
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
                                                Timber.e("Fail", volleyError);
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
                                                Timber.e("Fail", volleyError);
                                            }
                                        }
                                );
                                req.execute();

                                setHomeGdg(mPreferences.getString(Const.SETTINGS_HOME_GDG, ""));
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
        mGoogleApiClient = ((GdgActivity)getActivity()).getGoogleApiClient();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferenceManager = getPreferenceManager();
        mPreferenceManager.setSharedPreferencesName("gdg");

        mXClient = new GdgX();
        mGcm = GoogleCloudMessaging.getInstance(getActivity());

        mPreferences = mPreferenceManager.getSharedPreferences();

        addPreferencesFromResource(R.xml.settings);

        initPreferences();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mLoading = new LinearLayout(getActivity());
    }

    private void initPreferences() {
        final ListPreference prefHomeGdgList = (ListPreference) findPreference(Const.SETTINGS_HOME_GDG);
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

        CheckBoxPreference prefGcm = (CheckBoxPreference) findPreference(Const.SETTINGS_GCM);
        if (prefGcm != null) {
            prefGcm.setOnPreferenceChangeListener(mOnGcmPreferenceChange);
        }

        CheckBoxPreference prefGoogleSignIn = (CheckBoxPreference) findPreference("gdg_signed_in");
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

        CheckBoxPreference prefAnalytics = (CheckBoxPreference) findPreference("analytics");
        if (prefAnalytics != null) {
            prefAnalytics.setOnPreferenceChangeListener(mOnAnalyticsPreferenceChange);
        }
    }

    private void setHomeGdg(final String homeGdg) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                String token = null;
                try {
                    token = GoogleAuthUtil.getToken(getActivity(), Plus.AccountApi.getAccountName(((GdgActivity)getActivity()).getGoogleApiClient()), "oauth2: " + Scopes.PLUS_LOGIN);
                    mXClient.setToken(token);

                    mXClient.setHomeGdg(homeGdg, null, null).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    // TODO: Re-Implement with GMS 4.3
    public void onSignInFailed() {
        Timber.d("onSignInFailed");
        mPreferences.edit().putBoolean(Const.SETTINGS_SIGNED_IN, false).apply();
        CheckBoxPreference prefGoogleSignIn = (CheckBoxPreference) findPreference("gdg_signed_in");
        if (prefGoogleSignIn != null) {
            prefGoogleSignIn.setChecked(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);
    }
}
