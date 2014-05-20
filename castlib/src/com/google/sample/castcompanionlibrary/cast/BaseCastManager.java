/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.castcompanionlibrary.cast;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGD;
import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.RemoteControlClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.app.MediaRouteDialogFactory;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.sample.castcompanionlibrary.R;
import com.google.sample.castcompanionlibrary.cast.callbacks.BaseCastConsumerImpl;
import com.google.sample.castcompanionlibrary.cast.callbacks.IBaseCastConsumer;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.OnFailedListener;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract class that manages connectivity to a cast device. Subclasses are expected to extend
 * the functionality of this class based on their purpose.
 */
public abstract class BaseCastManager implements DeviceSelectionListener, ConnectionCallbacks,
        OnConnectionFailedListener, OnFailedListener {

    /**
     * Enumerates various stages during a session recovery
     */
    public static enum ReconnectionStatus {
        STARTED, IN_PROGRESS, FINALIZE, INACTIVE;
    }

    public static final int FEATURE_DEBUGGING = 1;
    public static final int FEATURE_NOTIFICATION = 4;
    public static final int FEATURE_LOCKSCREEN = 2;
    public static final String PREFS_KEY_SESSION_ID = "session-id";
    public static final String PREFS_KEY_APPLICATION_ID = "application-id";
    public static final String PREFS_KEY_CAST_ACTIVITY_NAME = "cast-activity-name";
    public static final String PREFS_KEY_CAST_CUSTOM_DATA_NAMESPACE = "cast-custom-data-namespace";
    public static final String PREFS_KEY_VOLUME_INCREMENT = "volume-increment";
    public static final String PREFS_KEY_ROUTE_ID = "route-id";

    public static final int NO_STATUS_CODE = -1;

    private static String CCL_VERSION;

    private static final String TAG = LogUtils.makeLogTag(BaseCastManager.class);
    private static final int SESSION_RECOVERY_TIMEOUT = 5; // in seconds

    protected Context mContext;
    protected MediaRouter mMediaRouter;
    protected MediaRouteSelector mMediaRouteSelector;
    protected CastMediaRouterCallback mMediaRouterCallback;
    protected CastDevice mSelectedCastDevice;
    protected String mDeviceName;
    private final Set<IBaseCastConsumer> mBaseCastConsumers = Collections
            .synchronizedSet(new HashSet<IBaseCastConsumer>());
    private boolean mDestroyOnDisconnect = false;
    protected String mApplicationId;
    protected Handler mHandler;
    protected ReconnectionStatus mReconnectionStatus = ReconnectionStatus.INACTIVE;
    protected int mVisibilityCounter;
    protected boolean mUiVisible;
    protected GoogleApiClient mApiClient;
    protected AsyncTask<Void, Integer, Integer> mReconnectionTask;
    protected int mCapabilities;
    protected boolean mConnectionSuspened;
    private boolean mWifiConnectivity = true;
    protected static BaseCastManager mCastManager;
    protected String mSessionId;

    /*************************************************************************/
    /************** Abstract Methods *****************************************/
    /*************************************************************************/

    /**
     * A chance for the subclasses to perform what needs to be done when a route is unselected. Most
     * of the logic is handled by the {@link BaseCastManager} but each subclass may have some
     * additional logic that can be done, e.g. detaching data or media channels that they may have
     * set up.
     */
    abstract void onDeviceUnselected();

    /**
     * Since application lifecycle callbacks are managed by subclasses, this abstract method needs
     * to be implemented by each subclass independently.
     *
     * @param device
     * @return
     */
    abstract Cast.CastOptions.Builder getCastOptionBuilder(CastDevice device);

    /**
     * Subclasses can decide how the Cast Controller Dialog should be built. If this returns
     * <code>null</code>, the default dialog will be shown.
     *
     * @return
     */
    abstract MediaRouteDialogFactory getMediaRouteDialogFactory();

    /**
     * Subclasses should implement this to react appropriately to the successful launch of their
     * application. This is called when the application is successfully launched.
     *
     * @param applicationMetadata
     * @param applicationStatus
     * @param sessionId
     * @param wasLaunched
     */
    abstract void onApplicationConnected(ApplicationMetadata applicationMetadata,
            String applicationStatus, String sessionId, boolean wasLaunched);

    /**
     * Called when the launch of application has failed. Subclasses need to handle this by doing
     * appropriate clean up.
     *
     * @param statusCode
     */
    abstract void onApplicationConnectionFailed(int statusCode);

    /**
     * Called when the attempt to stop application has failed.
     *
     * @param statusCode
     */
    abstract void onApplicationStopFailed(int statusCode);

    /************************************************************************/

    protected BaseCastManager(Context context, String applicationId) {
        CCL_VERSION = context.getString(R.string.ccl_version);
        LOGD(TAG, "BaseCastManager is instantiated");
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mApplicationId = applicationId;
        Utils.saveStringToPreference(mContext, PREFS_KEY_APPLICATION_ID, applicationId);

        LOGD(TAG, "Application ID is: " + mApplicationId);
        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(
                CastMediaControlIntent.categoryForCast(mApplicationId)).build();

        mMediaRouterCallback = new CastMediaRouterCallback(this, context);
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    public void onWifiConnectivityChanged(boolean connected) {
        LOGD(TAG, "WIFI connectivity changed to " + (connected ? "enabled" : "disabled"));
        if (connected && !mWifiConnectivity) {
            mWifiConnectivity = true;
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    reconnectSessionIfPossible(mContext, false, 10);
                }
            }, 1000);

        } else {
            mWifiConnectivity = connected;
        }
    }

    public static BaseCastManager getCastManager() {
        return mCastManager;
    }

    /**
     * Sets the {@link Context} for the subsequent calls. Setting context can help the library to
     * show error messages to the user.
     *
     * @param context
     */
    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    public void onDeviceSelected(CastDevice device) {
        setDevice(device, mDestroyOnDisconnect);
    }

    /**
     * This is called from {@link com.google.sample.castcompanionlibrary.cast.CastMediaRouterCallback}
     * to signal the change in presence of cast devices on network.
     *
     * @param castPresent
     */
    public void onCastAvailabilityChanged(boolean castPresent) {
        if (null != mBaseCastConsumers) {
            synchronized (mBaseCastConsumers) {
                for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                    try {
                        consumer.onCastAvailabilityChanged(castPresent);
                    } catch (Exception e) {
                        LOGE(TAG, "onCastAvailabilityChanged(): Failed to inform " + consumer, e);
                    }
                }
            }
        }
    }

    public void setDevice(CastDevice device, boolean stopAppOnExit) {
        mSelectedCastDevice = device;
        mDeviceName = mSelectedCastDevice != null ? mSelectedCastDevice.getFriendlyName() : null;

        if (mSelectedCastDevice == null) {
            if (!mConnectionSuspened) {
                Utils.saveStringToPreference(mContext, PREFS_KEY_SESSION_ID, null);
                Utils.saveStringToPreference(mContext, PREFS_KEY_ROUTE_ID, null);
            }
            mConnectionSuspened = false;
            try {
                if (isConnected()) {
                    if (stopAppOnExit) {
                        LOGD(TAG, "Calling stopApplication");
                        stopApplication();
                    }
                }
            } catch (IllegalStateException e) {
                LOGE(TAG, "Failed to stop the application after disconnecting route", e);
            } catch (IOException e) {
                LOGE(TAG, "Failed to stop the application after disconnecting route", e);
            } catch (TransientNetworkDisconnectionException e) {
                LOGE(TAG, "Failed to stop the application after disconnecting route", e);
            } catch (NoConnectionException e) {
                LOGE(TAG, "Failed to stop the application after disconnecting route", e);
            }
            onDisconnected();
            onDeviceUnselected();
            if (null != mApiClient) {
                LOGD(TAG, "Trying to disconnect");
                mApiClient.disconnect();
                if (null != mMediaRouter) {
                    mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
                }
                mApiClient = null;
            }
            mSessionId = null;
        } else if (null == mApiClient) {
            LOGD(TAG, "acquiring a connection to Google Play services for " + mSelectedCastDevice);
            Cast.CastOptions.Builder apiOptionsBuilder = getCastOptionBuilder(mSelectedCastDevice);
            mApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mApiClient.connect();
        } else if (!mApiClient.isConnected()) {
            mApiClient.connect();
        }
    }

    @Override
    public void onCastDeviceDetected(RouteInfo info) {
        if (null != mBaseCastConsumers) {
            synchronized (mBaseCastConsumers) {
                for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                    try {
                        consumer.onCastDeviceDetected(info);
                    } catch (Exception e) {
                        LOGE(TAG, "onCastDeviceDetected(): Failed to inform " + consumer, e);
                    }
                }
            }
        }
    }

    /**
     * Adds and wires up the Media Router cast button. It returns a pointer to the Media Router menu
     * item if the caller needs such reference. It is assumed that the enclosing
     * {@link android.app.Activity} inherits (directly or indirectly) from
     * {@link android.support.v7.app.ActionBarActivity}.
     *
     * @param menu
     * @param menuResourceId The resource id of the cast button in the xml menu descriptor file
     * @return
     */
    public MenuItem addMediaRouterButton(Menu menu, int menuResourceId) {
        MenuItem mediaRouteMenuItem = menu.findItem(menuResourceId);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)
                MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        if (null != getMediaRouteDialogFactory()) {
            mediaRouteActionProvider.setDialogFactory(getMediaRouteDialogFactory());
        }
        return mediaRouteMenuItem;
    }

    /**
     * Adds and wires up the {@link android.support.v7.app.MediaRouteButton} instance that is passed
     * as an argument. This requires that
     * <ul>
     *     <li>The enclosing {@link android.app.Activity} inherits (directly or indirectly) from
     *     {@link android.support.v4.app.FragmentActivity}</li>
     *     <li>User adds the {@link android.support.v7.app.MediaRouteButton} to the layout and
     *     pass a reference to that instance to this method</li>
     *     <li>User is in charge of controlling the visibility of this button. However, this
     *     library makes it easier to do so: use the callback
     *     <code>onCastAvailabilityChanged(boolean)</code> to change the visibility of the button in
     *     your client. For example, extend
     *     {@link com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl}
     *     and override that method:
     *     <pre>
{@code
public void onCastAvailabilityChanged(boolean castPresent) {
    mMediaRouteButton.setVisibility(castPresent ? View.VISIBLE : View.INVISIBLE);
}
    }
     *     </pre>
     *     </li>
     * </ul>
     * @param button
     * @return
     */
    public MediaRouteButton addMediaRouterButton(MediaRouteButton button) {
        button.setRouteSelector(mMediaRouteSelector);
        if (null != getMediaRouteDialogFactory()) {
            button.setDialogFactory(getMediaRouteDialogFactory());
        }
        return button;
    }

    /*************************************************************************/
    /************** UI Visibility Management *********************************/
    /*************************************************************************/

    /**
     * Calling this method signals the library that an activity page is made visible. In common
     * cases, this should be called in the "onResume()" method of each activity of the application.
     * The library keeps a counter and when at least one page of the application becomes visible,
     * the {@link onUiVisibilityChanged()} method is called.
     */
    public synchronized void incrementUiCounter() {
        mVisibilityCounter++;
        if (!mUiVisible) {
            mUiVisible = true;
            onUiVisibilityChanged(true);
        }
        if (mVisibilityCounter == 0) {
            LOGD(TAG, "UI is no longer visible");
        } else {
            LOGD(TAG, "UI is visible");
        }
    }

    /**
     * Calling this method signals the library that an activity page is made invisible. In common
     * cases, this should be called in the "onPause()" method of each activity of the application.
     * The library keeps a counter and when all pages of the application become invisible, the
     * {@link onUiVisibilityChanged()} method is called.
     */
    public synchronized void decrementUiCounter() {
        if (--mVisibilityCounter == 0) {
            LOGD(TAG, "UI is no longer visible");
            if (mUiVisible) {
                mUiVisible = false;
                onUiVisibilityChanged(false);
            }
        } else {
            LOGD(TAG, "UI is visible");
        }
    }

    /**
     * This is called when UI visibility of the client has changed
     *
     * @param visible The updated visibility status
     */
    protected void onUiVisibilityChanged(boolean visible) {
        if (visible) {
            if (null != mMediaRouter && null != mMediaRouterCallback) {
                LOGD(TAG, "onUiVisibilityChanged() addCallback called");
                mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                        MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
            }
        } else {
            if (null != mMediaRouter) {
                LOGD(TAG, "onUiVisibilityChanged() removeCallback called");
                mMediaRouter.removeCallback(mMediaRouterCallback);
            }
        }
    }

    /*************************************************************************/
    /************** Utility Methods ******************************************/
    /*************************************************************************/

    /**
     * A utility method to validate that the appropriate version of the Google Play Services is
     * available on the device. If not, it will open a dialog to address the issue. The dialog
     * displays a localized message about the error and upon user confirmation (by tapping on
     * dialog) will direct them to the Play Store if Google Play services is out of date or missing,
     * or to system settings if Google Play services is disabled on the device.
     *
     * @param activity
     * @return
     */
    public static boolean checkGooglePlayServices(final Activity activity) {
        return Utils.checkGooglePlayServices(activity);
    }

    /**
     * @deprecated Use <code>checkGooglePlayServices</code>
     */
    public static boolean checkGooglePlaySevices(final Activity activity) {
        return checkGooglePlayServices(activity);
    }

    /**
     * can be used to find out if the application is connected to the service or not.
     *
     * @return <code>true</code> if connected, <code>false</code> otherwise.
     */
    public boolean isConnected() {
        return (null != mApiClient) && mApiClient.isConnected();
    }

    /**
     * Disconnects from the cast device and stops the application on the cast device.
     */
    public void disconnect() {
        if (isConnected()) {
            setDevice(null, false);
        }
    }

    /**
     * Returns the assigned human-readable name of the device, or <code>null</code> if no device is
     * connected.
     *
     * @return
     */
    public final String getDeviceName() {
        return mDeviceName;
    }

    /**
     * Sets a flag to control whether disconnection form a cast device should result in stopping the
     * running application or not. If <code>true</code> is passed, then application will be stopped.
     * Default behavior is not to stop the app.
     *
     * @param stopOnExit
     */
    public final void setStopOnDisconnect(boolean stopOnExit) {
        mDestroyOnDisconnect = stopOnExit;
    }

    /**
     * Returns the {@link MediaRouteSelector} object.
     *
     * @return
     */
    public final MediaRouteSelector getMediaRouteSelector() {
        return mMediaRouteSelector;
    }

    /**
     * Turns on configurable features in the library. All the supported features are turned off by
     * default and clients, prior to using them, need to turn them on; it is best to do is
     * immediately after initialization of the library. Bitwise OR combination of features should be
     * passed in if multiple features are needed
     * <p/>
     * Current set of configurable features are:
     * <ul>
     * <li>FEATURE_DEBUGGING : turns on debugging in Google Play services
     * <li>FEATURE_NOTIFICATION : turns notifications on
     * <li>FEATURE_LOCKSCREEN : turns on Lock Screen using {@link RemoteControlClient} in supported
     * versions (JB+)
     * </ul>
     *
     * @param capabilities
     */
    public void enableFeatures(int capabilities) {
        mCapabilities = capabilities;
    }

    /*
     * Returns true if and only if the feature is turned on
     */
    protected boolean isFeatureEnabled(int feature) {
        return (feature & mCapabilities) > 0;
    }

    /**
     * Sets the device (system) volume.
     *
     * @param volume Should be a value between 0 and 1, inclusive.
     * @throws CastException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void setDeviceVolume(double volume) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        try {
            Cast.CastApi.setVolume(mApiClient, volume);
        } catch (Exception e) {
            LOGE(TAG, "Failed to set volume", e);
            throw new CastException("Failed to set volume");
        }
    }

    /**
     * Gets the remote's system volume, a number between 0 and 1, inclusive.
     *
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public final double getDeviceVolume() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        return Cast.CastApi.getVolume(mApiClient);
    }

    /**
     * Increments (or decrements) the device volume by the given amount.
     *
     * @param delta
     * @throws CastException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void incrementDeviceVolume(double delta) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        double vol = getDeviceVolume();
        if (vol >= 0) {
            setDeviceVolume(vol + delta);
        }
    }

    /**
     * Returns <code>true</code> if remote device is muted. It internally determines if this should
     * be done for <code>stream</code> or <code>device</code> volume.
     *
     * @return
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public final boolean isDeviceMute() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        checkConnectivity();
        return Cast.CastApi.isMute(mApiClient);
    }

    /**
     * Mutes or un-mutes the device volume.
     *
     * @param mute
     * @throws CastException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void setDeviceMute(boolean mute) throws CastException,
            TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        try {
            Cast.CastApi.setMute(mApiClient, mute);
        } catch (Exception e) {
            LOGE(TAG, "Failed to set mute to: " + mute, e);
            throw new CastException("Failed to mute");
        }
    }

    /**
     * Clears the {@link android.content.Context}. Should be used when the client application is
     * being destroyed to avoid context leak.
     */
    public void clearContext(){
        this.mContext = null;
    }

    /**
     * Clears the {@link android.content.Context} if the current context is the same as the one
     * provided in the argument <code>context</code>. Should be used when the client application
     * is being destroyed to avoid context leak.
     *
     * @param context
     */
    public void clearContext(Context context){
        if (null != this.mContext && this.mContext == context) {
            LOGD(TAG, "Cleared context: " + context);
            this.mContext = null;
        }
    }

    /*************************************************************************/
    /************** Session Recovery Methods *********************************/
    /*************************************************************************/

    /**
     * Returns the current {@link ReconnectionStatus}
     *
     * @return
     */
    public ReconnectionStatus getReconnectionStatus() {
        return mReconnectionStatus;
    }

    /**
     * Sets the {@link ReconnectionStatus}
     *
     * @param status
     */
    public final void setReconnectionStatus(ReconnectionStatus status) {
        mReconnectionStatus = status;
    }

    /**
     * Returns <code>true</code> if there is enough persisted information to attempt a session
     * recovery. For this to return <code>true</code>, there needs to be persisted session ID and
     * route ID from the last successful launch.
     *
     * @param context
     * @return
     */
    public final boolean canConsiderSessionRecovery(Context context) {
        String sessionId = Utils.getStringFromPreference(context, PREFS_KEY_SESSION_ID);
        String routeId = Utils.getStringFromPreference(context, PREFS_KEY_ROUTE_ID);
        if (null == sessionId || null == routeId) {
            return false;
        }
        LOGD(TAG, "Found session info in the preferences, so proceed with an "
                + "attempt to reconnect if possible");
        return true;
    }

    private void reconnectSessionIfPossibleInternal(RouteInfo theRoute) {
        if (isConnected()) {
            return;
        }
        String sessionId = Utils.getStringFromPreference(mContext, PREFS_KEY_SESSION_ID);
        String routeId = Utils.getStringFromPreference(mContext, PREFS_KEY_ROUTE_ID);
        LOGD(TAG, "reconnectSessionIfPossible() Retrieved from preferences: " + "sessionId="
                + sessionId + ", routeId=" + routeId);
        if (null == sessionId || null == routeId) {
            return;
        }
        mReconnectionStatus = ReconnectionStatus.IN_PROGRESS;
        CastDevice device = CastDevice.getFromBundle(theRoute.getExtras());

        if (null != device) {
            LOGD(TAG, "trying to acquire Cast Client for " + device);
            onDeviceSelected(device);
        }
    }

    /*
     * Cancels the task responsible for recovery of prior sessions, is used internally.
     */
    void cancelReconnectionTask() {
        LOGD(TAG, "cancelling reconnection task");
        if (null != mReconnectionTask && !mReconnectionTask.isCancelled()) {
            mReconnectionTask.cancel(true);
        }
    }

    /**
     * This method tries to automatically re-establish connection to a session if
     * <ul>
     * <li>User had not done a manual disconnect in the last session
     * <li>The Cast Device that user had connected to previously is still running the same session
     * </ul>
     * Under these conditions, a best-effort attempt will be made to continue with the same session.
     * This attempt will go on for <code>timeoutInSeconds</code> seconds. During this period, an
     * optional dialog can be shown if <code>showDialog</code> is set to <code>true</code>. The
     * message in this dialog can be changed by overriding the resource
     * <code>R.string.session_reconnection_attempt</code>
     *
     * @param context
     * @param showDialog
     * @param timeoutInSeconds
     */
    public void reconnectSessionIfPossible(final Context context, final boolean showDialog,
            final int timeoutInSeconds) {
        if (isConnected()) {
            return;
        }
        LOGD(TAG, "reconnectSessionIfPossible()");
        String routeId = Utils.getStringFromPreference(context, PREFS_KEY_ROUTE_ID);
        if (canConsiderSessionRecovery(context)) {
            List<RouteInfo> routes = mMediaRouter.getRoutes();
            RouteInfo theRoute = null;
            if (null != routes && !routes.isEmpty()) {
                for (RouteInfo route : routes) {
                    if (route.getId().equals(routeId)) {
                        theRoute = route;
                        break;
                    }
                }
            }
            if (null != theRoute) {
                // route has already been discovered, so lets just get the
                // device, etc
                reconnectSessionIfPossibleInternal(theRoute);
            } else {
                // we set a flag so if the route is discovered within a short
                // period, we let onRouteAdded callback of
                // CastMediaRouterCallback take
                // care of that
                mReconnectionStatus = ReconnectionStatus.STARTED;
            }

            // we may need to reconnect to an existing session
            mReconnectionTask = new AsyncTask<Void, Integer, Integer>() {
                private ProgressDialog dlg;
                private final int SUCCESS = 1;
                private final int FAILED = 2;

                @Override
                protected void onCancelled() {
                    if (null != dlg) {
                        dlg.dismiss();
                    }
                    super.onCancelled();
                }

                @Override
                protected void onPreExecute() {
                    if (!showDialog) {
                        return;
                    }
                    dlg = new ProgressDialog(context);
                    dlg.setMessage(context.getString(R.string.session_reconnection_attempt));
                    dlg.setIndeterminate(true);
                    dlg.setCancelable(true);
                    dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            switch (mReconnectionStatus) {
                                case STARTED:
                                case IN_PROGRESS:
                                case FINALIZE:
                                    mReconnectionStatus = ReconnectionStatus.INACTIVE;
                                    onDeviceSelected(null);
                                    break;
                                default:
                                    break;
                            }
                            mReconnectionStatus = ReconnectionStatus.INACTIVE;
                            if (null != dlg) {
                                dlg.dismiss();
                            }
                            mReconnectionTask.cancel(true);
                        }
                    });
                    dlg.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (mReconnectionStatus) {
                                        case STARTED:
                                        case IN_PROGRESS:
                                        case FINALIZE:
                                            mReconnectionStatus = ReconnectionStatus.INACTIVE;
                                            onDeviceSelected(null);
                                            break;
                                        default:
                                            break;
                                    }
                                    mReconnectionStatus = ReconnectionStatus.INACTIVE;
                                    if (null != dlg) {
                                        dlg.cancel();
                                    }
                                    mReconnectionTask.cancel(true);
                                }
                            }
                    );
                    dlg.show();
                }

                @Override
                protected Integer doInBackground(Void... params) {
                    for (int i = 0; i < timeoutInSeconds; i++) {
                        if (mReconnectionTask.isCancelled()) {
                            if (null != dlg) {
                                dlg.dismiss();
                            }
                            return SUCCESS;
                        }
                        try {
                            if (isConnected()) {
                                cancel(true);
                            }
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    return FAILED;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    if (showDialog && null != dlg) {
                        dlg.dismiss();
                    }
                    if (null != result) {
                        if (result == FAILED) {
                            mReconnectionStatus = ReconnectionStatus.INACTIVE;
                            onDeviceSelected(null);
                        }
                    }
                }

            };
            mReconnectionTask.execute();
        }
    }

    /**
     * This method tries to automatically re-establish re-establish connection to a session if
     * <ul>
     * <li>User had not done a manual disconnect in the last session
     * <li>Device that user had connected to previously is still running the same session
     * </ul>
     * Under these conditions, a best-effort attempt will be made to continue with the same session.
     * This attempt will go on for 5 seconds. During this period, an optional dialog can be shown if
     * <code>showDialog</code> is set to <code>true
     * </code>.
     *
     * @param context
     * @param showDialog if set to <code>true</code>, a dialog will be shown
     */
    public void reconnectSessionIfPossible(final Context context, final boolean showDialog) {
        LOGD(TAG, "Context for calling reconnectSessionIfPossible(): " + context);
        reconnectSessionIfPossible(context, showDialog, SESSION_RECOVERY_TIMEOUT);
    }

    /************************************************************/
    /***** GoogleApiClient.ConnectionCallbacks ******************/
    /************************************************************/
    /**
     * This is called by the library when a connection is re-established after a transient
     * disconnect. Note: this is not called by SDK.
     */
    public void onConnectivityRecovered() {
        if (null != mBaseCastConsumers) {
            synchronized (mBaseCastConsumers) {
                for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                    try {
                        consumer.onConnectivityRecovered();
                    } catch (Exception e) {
                        LOGE(TAG, "onConnectivityRecovered: Failed to inform " + consumer, e);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.google.android.gms.GoogleApiClient.ConnectionCallbacks#onConnected
     * (android.os.Bundle)
     */
    @Override
    public void onConnected(Bundle arg0) {
        LOGD(TAG, "onConnected() reached with prior suspension: " + mConnectionSuspened);
        if (mConnectionSuspened) {
            mConnectionSuspened = false;
            onConnectivityRecovered();
            return;
        }
        if (!isConnected()) {
            if (mReconnectionStatus == ReconnectionStatus.IN_PROGRESS) {
                mReconnectionStatus = ReconnectionStatus.INACTIVE;
            }
            return;
        }
        try {
            Cast.CastApi.requestStatus(mApiClient);
            launchApp();

            if (null != mBaseCastConsumers) {
                synchronized (mBaseCastConsumers) {
                    for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                        try {
                            consumer.onConnected();
                        } catch (Exception e) {
                            LOGE(TAG, "onConnected: Failed to inform " + consumer, e);
                        }
                    }
                }
            }

        } catch (IOException e) {
            LOGE(TAG, "error requesting status", e);
        } catch (IllegalStateException e) {
            LOGE(TAG, "error requesting status", e);
        } catch (TransientNetworkDisconnectionException e) {
            LOGE(TAG, "error requesting status due to network issues", e);
        } catch (NoConnectionException e) {
            LOGE(TAG, "error requesting status due to network issues", e);
        }

    }

    /*
     * Note: this is not called by the SDK anymore but this library calls this in the appropriate
     * time.
     */
    protected void onDisconnected() {
        LOGD(TAG, "onDisconnected() reached");
        mDeviceName = null;
        if (null != mBaseCastConsumers) {
            synchronized (mBaseCastConsumers) {
                for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                    try {
                        consumer.onDisconnected();
                    } catch (Exception e) {
                        LOGE(TAG, "onDisconnected(): Failed to inform " + consumer, e);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see com.google.android.gms.GoogleApiClient.OnConnectionFailedListener#
     * onConnectionFailed(com.google.android.gms.common.ConnectionResult)
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        LOGD(TAG, "onConnectionFailed() reached, error code: " + result.getErrorCode()
                + ", reason: " + result.toString());
        mConnectionSuspened = false;
        setDevice(null, mDestroyOnDisconnect);
        if (null != mMediaRouter) {
            mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        }
        boolean showError = false;
        if (null != mBaseCastConsumers) {
            synchronized (mBaseCastConsumers) {
                for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                    try {
                        consumer.onConnectionFailed(result);
                    } catch (Exception e) {
                        LOGE(TAG, "onConnectionFailed(): Failed to inform " + consumer, e);
                    }
                }
            }
        }
        if (showError) {
            Utils.showErrorDialog(mContext, R.string.failed_to_connect);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mConnectionSuspened = true;
        LOGD(TAG, "onConnectionSuspended() was called with cause: " + cause);
        if (null != mBaseCastConsumers) {
            synchronized (mBaseCastConsumers) {
                for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                    try {
                        consumer.onConnectionSuspended(cause);
                    } catch (Exception e) {
                        LOGE(TAG, "onConnectionSuspended(): Failed to inform " + consumer, e);
                    }
                }
            }
        }
    }

    /*
     * Launches application. For this succeed, a connection should be already established by the
     * CastClient.
     */
    private void launchApp() throws TransientNetworkDisconnectionException, NoConnectionException {
        LOGD(TAG, "launchApp() is called");
        if (!isConnected()) {
            if (mReconnectionStatus == ReconnectionStatus.IN_PROGRESS) {
                mReconnectionStatus = ReconnectionStatus.INACTIVE;
                return;
            }
            checkConnectivity();
        }

        if (mReconnectionStatus == ReconnectionStatus.IN_PROGRESS) {
            LOGD(TAG, "Attempting to join a previously interrupted session...");
            String sessionId = Utils.getStringFromPreference(mContext, PREFS_KEY_SESSION_ID);
            LOGD(TAG, "joinApplication() -> start");
            Cast.CastApi.joinApplication(mApiClient, mApplicationId, sessionId).setResultCallback(
                    new ResultCallback<Cast.ApplicationConnectionResult>() {

                        @Override
                        public void onResult(ApplicationConnectionResult result) {
                            if (result.getStatus().isSuccess()) {
                                LOGD(TAG, "joinApplication() -> success");
                                onApplicationConnected(result.getApplicationMetadata(),
                                        result.getApplicationStatus(), result.getSessionId(),
                                        result.getWasLaunched());
                            } else {
                                LOGD(TAG, "joinApplication() -> failure");
                                onApplicationConnectionFailed(result.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        } else {
            LOGD(TAG, "Launching app");
            Cast.CastApi.launchApplication(mApiClient, mApplicationId).setResultCallback(
                    new ResultCallback<Cast.ApplicationConnectionResult>() {

                        @Override
                        public void onResult(ApplicationConnectionResult result) {
                            if (result.getStatus().isSuccess()) {
                                LOGD(TAG, "launchApplication() -> success result");
                                onApplicationConnected(result.getApplicationMetadata(),
                                        result.getApplicationStatus(), result.getSessionId(),
                                        result.getWasLaunched());
                            } else {
                                LOGD(TAG, "launchApplication() -> failure result");
                                onApplicationConnectionFailed(result.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }
    }

    /**
     * Stops the application on the receiver device.
     *
     * @throws IllegalStateException
     * @throws IOException
     * @throws NoConnectionException
     * @throws TransientNetworkDisconnectionException
     */
    public void stopApplication() throws IllegalStateException, IOException,
            TransientNetworkDisconnectionException, NoConnectionException {
        checkConnectivity();
        Cast.CastApi.stopApplication(mApiClient, mSessionId).setResultCallback(new ResultCallback<Status>() {

            @Override
            public void onResult(Status result) {
                if (!result.isSuccess()) {
                    LOGD(TAG, "stopApplication -> onResult: stopping " + "application failed");
                    onApplicationStopFailed(result.getStatusCode());
                } else {
                    LOGD(TAG, "stopApplication -> onResult Stopped application " + "successfully");
                }
            }
        });
    }

    /*************************************************************/
    /***** Registering IBaseCastConsumer listeners ***************/
    /*************************************************************/
    /**
     * Registers an {@link IBaseCastConsumer} interface with this class. Registered listeners will
     * be notified of changes to a variety of lifecycle callbacks that the interface provides.
     *
     * @see BaseCastConsumerImpl
     * @param listener
     */
    public void addBaseCastConsumer(IBaseCastConsumer listener) {
        if (null != listener) {
            synchronized (mBaseCastConsumers) {
                if (mBaseCastConsumers.add(listener)) {
                    LOGD(TAG, "Successfully added the new BaseCastConsumer listener " + listener);
                }
            }
        }
    }

    /**
     * Unregisters an {@link IBaseCastConsumer}.
     *
     * @param listener
     */
    public void removeBaseCastConsumer(IBaseCastConsumer listener) {
        if (null != listener) {
            synchronized (mBaseCastConsumers) {
                if (mBaseCastConsumers.remove(listener)) {
                    LOGD(TAG, "Successfully removed the existing BaseCastConsumer listener " +
                            listener);
                }
            }
        }
    }

    /**
     * A simple method that throws an exception of there is no connectivity to the cast device.
     *
     * @throws TransientNetworkDisconnectionException If framework is still trying to recover
     * @throws NoConnectionException                  If no connectivity to the device exists
     */
    public void checkConnectivity() throws TransientNetworkDisconnectionException,
            NoConnectionException {
        if (!isConnected()) {
            if (mConnectionSuspened) {
                throw new TransientNetworkDisconnectionException();
            } else {
                throw new NoConnectionException();
            }
        }
    }

    @Override
    public void onFailed(int resourceId, int statusCode) {
        LOGD(TAG, "onFailed() was called with statusCode: " + statusCode);
        if (null != mBaseCastConsumers) {
            synchronized (mBaseCastConsumers) {
                for (IBaseCastConsumer consumer : mBaseCastConsumers) {
                    try {
                        consumer.onFailed(resourceId, statusCode);
                    } catch (Exception e) {
                        LOGE(TAG, "onFailed(): Failed to inform " + consumer, e);
                    }
                }
            }
        }

    }

    /**
     * Returns the version of this library.
     *
     * @return
     */
    public final static String getCclVersion() {
        return CCL_VERSION;
    }
}
