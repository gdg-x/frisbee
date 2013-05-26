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

package org.gdg.frisbee.android.view;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import org.gdg.frisbee.android.app.App;
import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;
import uk.co.senab.bitmapcache.CacheableImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.RejectedExecutionException;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.view
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 04:20
 */
public class NetworkedCacheableImageView extends CacheableImageView {

    /**
     * This task simply fetches an Bitmap from the specified URL and wraps it in a wrapper. This
     * implementation is NOT 'best practice' or production ready code.
     */
    private static class ImageUrlAsyncTask extends AsyncTask<String, Void, CacheableBitmapDrawable> {

        private Animation mFadeInAnimation;
        private Animation mFadeOutAnimation;

        private final BitmapLruCache mCache;

        private final WeakReference<ImageView> mImageViewRef;

        private final BitmapFactory.Options mDecodeOpts;

        ImageUrlAsyncTask(ImageView imageView, BitmapLruCache cache,
                          BitmapFactory.Options decodeOpts) {
            mCache = cache;
            mImageViewRef = new WeakReference<ImageView>(imageView);
            mDecodeOpts = decodeOpts;

            mFadeInAnimation = new AlphaAnimation(0,1);
            mFadeInAnimation.setDuration(500);

            mFadeOutAnimation = new AlphaAnimation(1,0);
            mFadeOutAnimation.setDuration(500);
        }

        @Override
        protected CacheableBitmapDrawable doInBackground(String... params) {
            try {
                // Return early if the ImageView has disappeared.
                if (null == mImageViewRef.get()) {
                    return null;
                }

                String url = params[0];
                if(url.startsWith("//")) {
                    url = "http:"+url;
                }
                // Now we're not on the activity_main thread we can check all caches
                CacheableBitmapDrawable result = mCache.get(url, mDecodeOpts);

                if (null == result) {
                    // The bitmap isn't cached so download from the web
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    InputStream is = new BufferedInputStream(conn.getInputStream());

                    // Add to cache
                    result = mCache.put(url, is, mDecodeOpts);
                }

                return result;

            } catch (IOException e) {
                Log.e("ImageUrlAsyncTask", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(final CacheableBitmapDrawable result) {
            super.onPostExecute(result);

            if(result == null) {
                return;
            }

            final ImageView iv = mImageViewRef.get();

            if (null != iv) {

                mFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        iv.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                if(iv.getVisibility() == View.VISIBLE) {
                    mFadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            if(!result.getBitmap().isRecycled()) {
                                iv.setImageDrawable(result);
                                iv.startAnimation(mFadeInAnimation);
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    iv.startAnimation(mFadeOutAnimation);
                } else {
                    if(!result.getBitmap().isRecycled()) {
                        iv.setImageDrawable(result);
                        iv.startAnimation(mFadeInAnimation);
                    }

                }
                Log.d("MasonryView","loaded");
            }
        }
    }

    private final BitmapLruCache mCache;

    private ImageUrlAsyncTask mCurrentTask;

    public NetworkedCacheableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCache = App.getInstance().getBitmapCache();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        final Drawable previousDrawable = getDrawable();

        if (drawable != previousDrawable) {
            onDrawableSet(drawable);
            onDrawableUnset(previousDrawable);
        }

        // Set new Drawable
        super.setImageDrawable(drawable);
    }

    private static void onDrawableSet(Drawable drawable) {
        if (drawable instanceof CacheableBitmapDrawable) {
            ((CacheableBitmapDrawable) drawable).setBeingUsed(true);
        }
    }

    private static void onDrawableUnset(final Drawable drawable) {
        if (drawable instanceof CacheableBitmapDrawable) {
            ((CacheableBitmapDrawable) drawable).setBeingUsed(false);
        }
    }

    /**
     * Loads the Bitmap.
     *
     * @param url      - URL of image
     * @param fullSize - Whether the image should be kept at the original size
     * @return true if the bitmap was found in the cache
     */
    public boolean loadImage(String url, final boolean fullSize) {
        // First check whether there's already a task running, if so cancel it
        if (null != mCurrentTask) {
            mCurrentTask.cancel(true);
        }

        // Check to see if the memory cache already has the bitmap. We can
        // safely do
        // this on the activity_main thread.
        BitmapDrawable wrapper = mCache.getFromMemoryCache(url);

        if (null != wrapper) {
            // The cache has it, so just display it
            setImageDrawable(wrapper);
            return true;
        } else {
            // Memory Cache doesn't have the URL, do threaded request...
            setImageDrawable(null);

            BitmapFactory.Options decodeOpts = null;

            if (!fullSize) {
                decodeOpts = new BitmapFactory.Options();
                decodeOpts.inDensity = DisplayMetrics.DENSITY_XHIGH;
            }

            mCurrentTask = new ImageUrlAsyncTask(this, mCache, decodeOpts);

            try {
                mCurrentTask.execute(url);
            } catch (RejectedExecutionException e) {
                // This shouldn't happen, but might.
            }

            return false;
        }
    }
}