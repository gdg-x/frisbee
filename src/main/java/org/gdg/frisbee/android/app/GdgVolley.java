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

package org.gdg.frisbee.android.app;

import android.content.Context;
import android.graphics.Bitmap;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gdg.frisbee.android.api.GdgStack;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.api
 * <p/>
 * User: maui
 * Date: 26.05.13
 * Time: 19:25
 */
public class GdgVolley {

    private static GdgVolley mInstance;
    public static GdgVolley getInstance() {
        return mInstance;
    }

    private RequestQueue mRequestQueue, mImageRequestQueue;
    private ImageLoader mImageLoader;

    static void init(Context ctx) {
        mInstance = new GdgVolley(ctx);
    }

    private GdgVolley(Context ctx) {
        mRequestQueue = Volley.newRequestQueue(ctx, new GdgStack());
        mImageRequestQueue = Volley.newRequestQueue(ctx, new GdgStack());
        mImageLoader = new ImageLoader(mImageRequestQueue, new ImageLoader.ImageCache() {
            @Override
            public Bitmap getBitmap(String url) {
                // Check memcache here...disk cache lookup will be done by GdgStack
                CacheableBitmapDrawable bitmap = App.getInstance().getBitmapCache().getFromMemoryCache(url);
                if(bitmap != null)
                    return bitmap.getBitmap();
                else
                    return null;
            }

            @Override
            public void putBitmap(final String url, final Bitmap bitmap) {
                new Thread() {
                    @Override
                    public void run() {
                        App.getInstance().getBitmapCache().put(url, bitmap);
                    }
                }.start();
            }
        });
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

    public ImageLoader getImageLoader() {
        if (mImageLoader != null) {
            return mImageLoader;
        } else {
            throw new IllegalStateException("ImageLoader not initialized");
        }
    }
}
