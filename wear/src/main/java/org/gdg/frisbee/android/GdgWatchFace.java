/*
 * Copyright (C) 2014 The Android Open Source Project
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

package org.gdg.frisbee.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't shown. On
 * devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient mode.
 */
public class GdgWatchFace extends CanvasWatchFaceService {
    private static final String TAG = "GdgWatchFace";
    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener {

        private static final float HAND_END_CAP_RADIUS = 5f;
        /**
         * Handler to update the time once a second in interactive mode.
         */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (R.id.message_update_time == message.what) {
                    invalidate();
                    if (shouldTimerBeRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = INTERACTIVE_UPDATE_RATE_MS
                            - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                        mUpdateTimeHandler.sendEmptyMessageDelayed(R.id.message_update_time, delayMs);
                    }
                }
            }
        };
        Paint mBackgroundPaint;
        Paint mHourHandPaint;
        Paint mMinuteHandPaint;
        Paint mSecondHandPaint;
        Paint mHourMarkerPaint;
        Paint mTextPaint;
        Bitmap mBackgroundBitmap;
        Bitmap mDarkBackgroundBitmap;
        Bitmap mLightBackgroundBitmap;
        Bitmap mGrayBackgroundBitmap;
        boolean mAmbient;
        boolean mLightMode = false;
        boolean mDisplayDate = true;
        Time mTime;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        boolean mRegisteredTimeZoneReceiver = false;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;
        boolean mBurnInProtection;
        private Rect mCardBounds = new Rect();
        private float mHourHandLength;
        private float mMinuteHandLength;
        private float mSecondHandLength;
        private int mWidth;
        private int mHeight;
        private float mCenterX;
        private float mCenterY;
        private GoogleApiClient mGoogleApiClient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(GdgWatchFace.this)
                .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                .setShowSystemUiTime(false)
                .setViewProtection(WatchFaceStyle.PROTECT_STATUS_BAR | WatchFaceStyle.PROTECT_HOTWORD_INDICATOR)
                .setHotwordIndicatorGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL)
                .setStatusBarGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                .build());

            Resources resources = GdgWatchFace.this.getResources();

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(ContextCompat.getColor(GdgWatchFace.this, R.color.gdg_black));

            mBackgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.gdg_black);
            mDarkBackgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.gdg_black);
            mLightBackgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.gdg_background);

            mHourHandPaint = new Paint();
            mHourHandPaint.setColor(ContextCompat.getColor(GdgWatchFace.this, R.color.gdg_gray));
            mHourHandPaint.setStrokeWidth(resources.getDimension(R.dimen.watch_hand_stroke));
            mHourHandPaint.setAntiAlias(true);
            mHourHandPaint.setStrokeCap(Paint.Cap.ROUND);

            mMinuteHandPaint = new Paint();
            mMinuteHandPaint.setColor(ContextCompat.getColor(GdgWatchFace.this, R.color.gdg_gray));
            mMinuteHandPaint.setStrokeWidth(resources.getDimension(R.dimen.watch_hand_stroke));
            mMinuteHandPaint.setAntiAlias(true);
            mMinuteHandPaint.setStrokeCap(Paint.Cap.ROUND);

            mSecondHandPaint = new Paint();
            mSecondHandPaint.setColor(ContextCompat.getColor(GdgWatchFace.this, R.color.gdg_white));
            mSecondHandPaint.setStrokeWidth(resources.getDimension(R.dimen.second_hand_stroke));
            mSecondHandPaint.setAntiAlias(true);
            mSecondHandPaint.setStrokeCap(Paint.Cap.ROUND);

            mHourMarkerPaint = new Paint();
            mHourMarkerPaint.setColor(Color.WHITE);
            mHourMarkerPaint.setStrokeWidth(resources.getDimension(R.dimen.hour_marker_stroke));
            mHourMarkerPaint.setAntiAlias(true);

            mTextPaint = new Paint();
            mTextPaint.setColor(ContextCompat.getColor(GdgWatchFace.this, R.color.gdg_white));
            mTextPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            mTextPaint.setTextSize(resources.getDimension(R.dimen.font_hour_marker));
            mTextPaint.setAntiAlias(true);

            mTime = new Time();

            mGoogleApiClient = new GoogleApiClient.Builder(GdgWatchFace.this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected:" + bundle);
                        onApiClientConnected();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended:" + i);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed");
                    }
                })
                .addApi(Wearable.API)
                .build();
        }

        private void onApiClientConnected() {
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            Wearable.DataApi.getDataItems(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(DataItemBuffer dataItems) {
                        for (DataItem item : dataItems) {
                            updateConfig(item);
                            invalidateIfNecessary();
                        }

                        dataItems.release();
                    }
                });
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(R.id.message_update_time);

            disconnectGoogleApiClient();

            super.onDestroy();
        }

        private void disconnectGoogleApiClient() {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient || mBurnInProtection) {
                    mHourHandPaint.setAntiAlias(!inAmbientMode);
                    mMinuteHandPaint.setAntiAlias(!inAmbientMode);
                    mSecondHandPaint.setAntiAlias(!inAmbientMode);
                    mHourMarkerPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            mWidth = width;
            mHeight = height;
            mCenterX = mWidth / 2f;
            mCenterY = mHeight / 2f;
            mHourHandLength = mCenterX * 0.3f;
            mMinuteHandLength = mCenterX * 0.5f;
            mSecondHandLength = mCenterX * 0.7f;

            float scale = ((float) width / (float) mBackgroundBitmap.getWidth());

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                (int) (mBackgroundBitmap.getWidth() * scale),
                (int) (mBackgroundBitmap.getHeight() * scale), true);
            mDarkBackgroundBitmap = Bitmap.createScaledBitmap(mDarkBackgroundBitmap,
                (int) (mDarkBackgroundBitmap.getWidth() * scale),
                (int) (mDarkBackgroundBitmap.getHeight() * scale), true);
            mLightBackgroundBitmap = Bitmap.createScaledBitmap(mLightBackgroundBitmap,
                (int) (mLightBackgroundBitmap.getWidth() * scale),
                (int) (mLightBackgroundBitmap.getHeight() * scale), true);
            if (!mBurnInProtection || !mLowBitAmbient) {
                initializeGrayBackgroundBitmap();
            }
        }

        private void initializeGrayBackgroundBitmap() {
            mGrayBackgroundBitmap = Bitmap.createBitmap(mBackgroundBitmap.getWidth(), mBackgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);

            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

            Paint grayPaint = new Paint();
            grayPaint.setColorFilter(filter);

            Canvas canvas = new Canvas(mGrayBackgroundBitmap);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, grayPaint);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            mTime.setToNow();

            // Draw the background.
            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);
            } else if (mAmbient) {//TODO gray ambient BG for light mode
                canvas.drawBitmap(mGrayBackgroundBitmap, 0, 0, mBackgroundPaint);
            } else {
                canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
            }

            float textHeightOffset = (mTextPaint.descent() + mTextPaint.ascent()) / 2f;
            float innerTickRadius = mCenterX - 25;
            float outerTickRadius = mCenterX;
            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                canvas.drawLine(mCenterX + innerX, mCenterY + innerY,
                    mCenterX + outerX, mCenterY + outerY, getAdjustedPaintColor(mHourMarkerPaint));

            }

            if (mDisplayDate) {
                canvas.drawText(formatTwoDigitNumber(mTime.monthDay), mCenterX + mMinuteHandLength,
                    mCenterY - textHeightOffset, getAdjustedPaintColor(mTextPaint));
            }

            canvas.drawText(formatTwoDigitNumber(mTime.hour) + ":" + formatTwoDigitNumber(mTime.minute),
                mCenterX - mSecondHandLength, mCenterY - textHeightOffset, getAdjustedPaintColor(mTextPaint));

            /*
             * These calculations reflect the rotation in degrees per unit of
             * time, e.g. 360 / 60 = 6 and 360 / 12 = 30
             */
            final float secondsRotation = mTime.second * 6f;
            final float minutesRotation = mTime.minute * 6f;
            // account for the offset of the hour hand due to minutes of the hour.
            final float hourHandOffset = mTime.minute / 2f;
            final float hoursRotation = (mTime.hour * 30) + hourHandOffset;

            // save the canvas state before we begin to rotate it
            canvas.save();

            canvas.rotate(hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(mCenterX, mCenterY - HAND_END_CAP_RADIUS, mCenterX,
                mCenterY - mHourHandLength, getAdjustedPaintColor(mHourHandPaint));

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(mCenterX, mCenterY - HAND_END_CAP_RADIUS, mCenterX,
                mCenterY - mMinuteHandLength, getAdjustedPaintColor(mMinuteHandPaint));

            if (!mAmbient) {
                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
                canvas.drawLine(mCenterX, mCenterY, mCenterX,
                    mCenterY - mSecondHandLength, mSecondHandPaint);
            }

            canvas.drawCircle(mCenterX, mCenterY, HAND_END_CAP_RADIUS, getAdjustedPaintColor(mHourHandPaint));

            // restore the canvas' original orientation.
            canvas.restore();

            if (mAmbient) {
                canvas.drawRect(mCardBounds, mBackgroundPaint);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                mGoogleApiClient.connect();
                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();

                disconnectGoogleApiClient();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            mCardBounds.set(rect);
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            GdgWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            GdgWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(R.id.message_update_time);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(R.id.message_update_time);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private Paint getAdjustedPaintColor(Paint paint) {
            if (mAmbient) {
                int ambientColor = mLightMode ? R.color.black : R.color.gdg_gray;

                paint = new Paint(paint);
                paint.setColor(ContextCompat.getColor(GdgWatchFace.this, ambientColor));
            }

            return paint;
        }

        private String formatTwoDigitNumber(int number) {
            return String.format(Locale.getDefault(), "%02d", number);
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                    DataItem item = dataEvent.getDataItem();
                    updateConfig(item);
                    invalidateIfNecessary();
                }
            }

            dataEvents.release();
        }

        private void updateConfig(DataItem item) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

            if (WearableConfigurationActivity.PATH_BACKGROUND.equals(item.getUri().getPath()) &&
                dataMap.containsKey(WearableConfigurationActivity.CONFIG_BACKGROUND)) {
                int background = dataMap.getInt(WearableConfigurationActivity.CONFIG_BACKGROUND);
                updateBackground(background);
            } else if (WearableConfigurationActivity.PATH_HOUR_HAND.equals(item.getUri().getPath()) &&
                dataMap.containsKey(WearableConfigurationActivity.CONFIG_HAND_HOUR)) {
                int color = dataMap.getInt(WearableConfigurationActivity.CONFIG_HAND_HOUR);
                updateHourHand(color);
            } else if (WearableConfigurationActivity.PATH_MINUTE_HAND.equals(item.getUri().getPath()) &&
                dataMap.containsKey(WearableConfigurationActivity.CONFIG_HAND_MINUTE)) {
                int color = dataMap.getInt(WearableConfigurationActivity.CONFIG_HAND_MINUTE);
                updateMinuteHand(color);
            } else if (WearableConfigurationActivity.PATH_SECOND_HAND.equals(item.getUri().getPath()) &&
                dataMap.containsKey(WearableConfigurationActivity.CONFIG_HAND_SECOND)) {
                int color = dataMap.getInt(WearableConfigurationActivity.CONFIG_HAND_SECOND);
                updateSecondHand(color);
            } else if (WearableConfigurationActivity.PATH_HOUR_MARKER.equals(item.getUri().getPath()) &&
                dataMap.containsKey(WearableConfigurationActivity.CONFIG_HOUR_MARKER)) {
                int color = dataMap.getInt(WearableConfigurationActivity.CONFIG_HOUR_MARKER);
                updateHourMarker(color);
            } else if (WearableConfigurationActivity.PATH_DATE.equals(item.getUri().getPath()) &&
                dataMap.containsKey(WearableConfigurationActivity.CONFIG_DATE)) {
                mDisplayDate = dataMap.getBoolean(WearableConfigurationActivity.CONFIG_DATE, true);
            }

        }

        private void updateBackground(int background) {
            if (background == Color.WHITE) {
                mBackgroundBitmap = mLightBackgroundBitmap;
            } else {
                mBackgroundBitmap = mDarkBackgroundBitmap;
            }

        }

        private void updateHourHand(int color) {
            mHourHandPaint.setColor(color);
        }

        private void updateMinuteHand(int color) {
            mMinuteHandPaint.setColor(color);
        }

        private void updateSecondHand(int color) {
            mSecondHandPaint.setColor(color);
        }

        private void updateHourMarker(int color) {
            mHourMarkerPaint.setColor(color);
        }

        private void invalidateIfNecessary() {
            if (isVisible() && !isInAmbientMode()) {
                invalidate();
            }
        }
    }
}
