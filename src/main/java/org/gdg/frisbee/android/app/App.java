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

import android.app.Application;
import com.github.ignition.support.cache.AbstractCache;
import org.gdg.frisbee.android.cache.ModelCache;
import uk.co.senab.bitmapcache.BitmapLruCache;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 20.04.13
 * Time: 12:09
 */
public class App extends Application {

    private static App mInstance = null;

    public static App getInstance() {
        return mInstance;
    }

    private BitmapLruCache mBitmapCache;
    private ModelCache mModelCache;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        GdgVolley.init(this);
    }

    public ModelCache getModelCache() {
        if(mModelCache == null) {
            mModelCache = new ModelCache(256, 15, 2);
            mModelCache.enableDiskCache(getApplicationContext(), AbstractCache.DISK_CACHE_SDCARD);
        }
        return mModelCache;
    }

    public BitmapLruCache getBitmapCache() {
        if(mBitmapCache == null)
            mBitmapCache = new BitmapLruCache.Builder(getApplicationContext())
                    .setDiskCacheEnabled(true)
                    .setMemoryCacheEnabled(true)
                    .build();

        return mBitmapCache;
    }
}
