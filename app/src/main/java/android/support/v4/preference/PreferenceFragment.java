package android.support.v4.preference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import timber.log.Timber;

/**
 * A PreferenceFragment for the support library. Based on the platform's code with some removed features and a basic ListView layout.
 *
 * @author Christophe Beyls
 */
public abstract class PreferenceFragment extends Fragment {

    private static final int FIRST_REQUEST_CODE = 100;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final String PREFERENCES_TAG = "android:preferences";
    private static final float HC_HORIZONTAL_PADDING = 16;
    
    private Handler mHandler;
    
    static class PreferenceHandler extends Handler {
        private final WeakReference<PreferenceScreen> mPreferenceScreenReference;
        private final WeakReference<ListView> mListReference;

        PreferenceHandler(PreferenceScreen preferenceScreen, ListView listView) {
            mPreferenceScreenReference = new WeakReference<>(preferenceScreen);
            mListReference = new WeakReference<>(listView);
        }
        
        @Override
        public void handleMessage(Message msg) {
            final ListView listView = mListReference.get();
            final PreferenceScreen preferenceScreen = mPreferenceScreenReference.get();
            if (listView != null && preferenceScreen != null) {
                switch (msg.what) {
                    case MSG_BIND_PREFERENCES:
                        bindPreferences(preferenceScreen, listView);
                        break;
                }
            }
        }
    }
    
    private boolean mHavePrefs;
    private boolean mInitDone;
    private ListView mList;
    private PreferenceManager mPreferenceManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Constructor<PreferenceManager> c = PreferenceManager.class.getDeclaredConstructor(Activity.class, int.class);
            c.setAccessible(true);
            mPreferenceManager = c.newInstance(this.getActivity(), FIRST_REQUEST_CODE);
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        mList = new ListView(getActivity());
        mList.setId(android.R.id.list);
        final int horizontalPadding = (int) (HC_HORIZONTAL_PADDING * getResources().getDisplayMetrics().density);
        mList.setPadding(horizontalPadding, 0, horizontalPadding, 0);
        return mList;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        mHandler = new PreferenceHandler(getPreferenceScreen(), mList);

        if (mHavePrefs) {
            bindPreferences(getPreferenceScreen(), mList);
        }

        mInitDone = true;

        if (savedInstanceState != null) {
            Bundle container = savedInstanceState.getBundle(PREFERENCES_TAG);
            if (container != null) {
                final PreferenceScreen preferenceScreen = getPreferenceScreen();
                if (preferenceScreen != null) {
                    preferenceScreen.restoreHierarchyState(container);
                }
            }
        }
    }

    public void onStop() {
        super.onStop();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityStop");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
        }
    }

    public void onDestroyView() {
        mList = null;
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityDestroy");
            m.setAccessible(true);
            m.invoke(mPreferenceManager);
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle(PREFERENCES_TAG, container);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("dispatchActivityResult", int.class, int.class, Intent.class);
            m.setAccessible(true);
            m.invoke(mPreferenceManager, requestCode, resultCode, data);
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
        }
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public void setPreferenceScreen(PreferenceScreen screen) {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("setPreferences", PreferenceScreen.class);
            m.setAccessible(true);
            boolean result = (Boolean) m.invoke(mPreferenceManager, screen);
            if (result && (screen != null)) {
                mHavePrefs = true;
                if (mInitDone) {
                    postBindPreferences();
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
        }
    }

    public PreferenceScreen getPreferenceScreen() {
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("getPreferenceScreen");
            m.setAccessible(true);
            return (PreferenceScreen) m.invoke(mPreferenceManager);
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
            return null;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("inflateFromIntent", Intent.class, PreferenceScreen.class);
            m.setAccessible(true);
            PreferenceScreen screen = (PreferenceScreen) m.invoke(mPreferenceManager, intent, getPreferenceScreen());
            setPreferenceScreen(screen);
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
        }
    }

    public void addPreferencesFromResource(int resId) {
        requirePreferenceManager();
        try {
            Method m = PreferenceManager.class.getDeclaredMethod("inflateFromResource", Context.class, int.class, PreferenceScreen.class);
            m.setAccessible(true);
            PreferenceScreen screen = (PreferenceScreen) m.invoke(mPreferenceManager, getActivity(), resId, getPreferenceScreen());
            setPreferenceScreen(screen);
        } catch (Exception e) {
            Timber.e(e, "Exception while trying to do reflection.");
        }
    }

    public Preference findPreference(CharSequence key) {
        if (mPreferenceManager == null) {
            return null;
        }
        return mPreferenceManager.findPreference(key);
    }

    private void requirePreferenceManager() {
        if (this.mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void postBindPreferences() {
        if (!mHandler.hasMessages(MSG_BIND_PREFERENCES)) {
            mHandler.sendEmptyMessage(MSG_BIND_PREFERENCES);
        }
    }

    private static void bindPreferences(PreferenceScreen preferenceScreen, ListView listView) {
        if (preferenceScreen != null) {
            preferenceScreen.bind(listView);
        }
    }
}
