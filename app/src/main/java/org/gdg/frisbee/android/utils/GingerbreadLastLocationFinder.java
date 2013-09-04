/*
 * Copyright 2011 Google Inc.
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

package org.gdg.frisbee.android.utils;


import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import org.gdg.frisbee.android.utils.base.ILastLocationFinder;

/**
 * Optimized implementation of Last Location Finder for devices running Gingerbread
 * and above.
 *
 * This class let's you find the "best" (most accurate and timely) previously
 * detected location using whatever providers are available.
 *
 * Where a timely / accurate previous location is not detected it will
 * return the newest location (where one exists) and setup a oneshot
 * location update to find the current location.
 */
public class GingerbreadLastLocationFinder implements ILastLocationFinder {

    protected static String TAG = "LastLocationFinder";
    protected static String SINGLE_LOCATION_UPDATE_ACTION = "org.gdg.frisbee.actions.SINGLE_LOCATION_UPDATE_ACTION";

    protected PendingIntent singleUpatePI;
    protected LocationListener locationListener;
    protected LocationManager locationManager;
    protected Context context;
    protected Criteria criteria;

    /**
     * Construct a new Gingerbread Last Location Finder.
     * @param context Context
     */
    public GingerbreadLastLocationFinder(Context context) {
        this.context = context;
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        // Coarse accuracy is specified here to get the fastest possible result.
        // The calling Activity will likely (or have already) request ongoing
        // updates using the Fine location provider.
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);

        // Construct the Pending Intent that will be broadcast by the oneshot
        // location update.
        Intent updateIntent = new Intent(SINGLE_LOCATION_UPDATE_ACTION);
        singleUpatePI = PendingIntent.getBroadcast(context, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Returns the most accurate and timely previously detected location.
     * Where the last result is beyond the specified maximum distance or
     * latency a one-off location update is returned via the {@link LocationListener}
     * specified in {@link setChangedLocationListener}.
     * @param minDistance Minimum distance before we require a location update.
     * @param minTime Minimum time required between location updates.
     * @return The most accurate and / or timely previously detected location.
     */
    public Location getLastBestLocation(int minDistance, long minTime) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider: matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                }
                else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                }
            }
        }

        // If the best result is beyond the allowed time limit, or the accuracy of the
        // best result is wider than the acceptable maximum distance, request a single update.
        // This check simply implements the same conditions we set when requesting regular
        // location updates every [minTime] and [minDistance].
        if (locationListener != null && (bestTime < minTime || bestAccuracy > minDistance)) {
            List<String> providers = locationManager.getProviders(criteria, true);
            try {
                if (providers != null && providers.size() > 0){

                        IntentFilter locIntentFilter = new IntentFilter(SINGLE_LOCATION_UPDATE_ACTION);
                        context.registerReceiver(singleUpdateReceiver, locIntentFilter);
                        locationManager.requestSingleUpdate(criteria, singleUpatePI);
                }
            } catch (SecurityException ex) {
                Log.e(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
        }

        return bestResult;
    }

    /**
     * This {@link BroadcastReceiver} listens for a single location
     * update before unregistering itself.
     * The oneshot location update is returned via the {@link LocationListener}
     * specified in {@link setChangedLocationListener}.
     */
    protected BroadcastReceiver singleUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.unregisterReceiver(singleUpdateReceiver);

            String key = LocationManager.KEY_LOCATION_CHANGED;
            Location location = (Location)intent.getExtras().get(key);

            if (locationListener != null && location != null)
                locationListener.onLocationChanged(location);

            locationManager.removeUpdates(singleUpatePI);
        }
    };

    /**
     * {@inheritDoc}
     */
    public void setChangedLocationListener(LocationListener l) {
        locationListener = l;
    }

    /**
     * {@inheritDoc}
     */
    public void cancel() {
        locationManager.removeUpdates(singleUpatePI);
    }
}