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

package org.gdg.frisbee.android.cache;

import android.util.Log;
import com.github.ignition.support.cache.CacheHelper;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.gdg.frisbee.android.api.deserializer.DateTimeDeserializer;
import org.gdg.frisbee.android.api.deserializer.DateTimeSerializer;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.cache
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 05:20
 */
public class ModelCache extends AbstractCache<String, Object> {

    private static final String LOG_TAG = "GDG-ModelCache";

    final JsonFactory mJsonFactory = new GsonFactory();

    private Gson mGson;

    public ModelCache(int initialCapacity, long expirationInMinutes, int maxConcurrentThreads) {
        super("ModelCache", initialCapacity, expirationInMinutes, maxConcurrentThreads);
        mGson = new GsonBuilder()
                .registerTypeAdapter(DateTime.class, new DateTimeDeserializer())
                .registerTypeAdapter(DateTime.class, new DateTimeSerializer())
                .create();
    }

    @Override
    public synchronized Object get(Object elementKey) {
        return super.get(elementKey);
    }

    @SuppressWarnings("unchecked")
    public synchronized Object get(int expirationInMinutes, Object elementKey) {
        String key = (String) elementKey;
        Object value = cache.get(key);
        if (value != null) {
            return value;
        }

        // memory miss, try reading from disk
        File file = getFileForKey(key);
        if (file.exists()) {
            // if file older than expirationInMinutes, remove it
            long lastModified = file.lastModified();
            Date now = new Date();
            long ageInMinutes = ((now.getTime() - lastModified) / (1000 * 60));

            if (ageInMinutes >= expirationInMinutes) {
                file.delete();
                return null;
            }

            // disk hit
            try {
                value = readValueFromDisk(file);
            } catch (IOException e) {
                // treat decoding errors as a cache miss
                e.printStackTrace();
                return null;
            }
            if (value == null) {
                return null;
            }
            cache.put(key, value);
            return value;
        }

        // cache miss
        return null;
    }

    private File getFileForKey(String key) {
        return new File(diskCacheDirectory + "/" + getFileNameForKey(key));
    }

    @Override
    public synchronized Object put(String key, Object value) {
        if (isDiskCacheEnabled()) {
            cacheToDisk(key, value);
        }

        return cache.put(key, value);
    }

    @Override
    public String getFileNameForKey(String url) {
        return CacheHelper.getFileNameFromUrl(url);
    }

    @Override
    protected Object readValueFromDisk(File file) throws IOException {
        FileReader fs = new FileReader(file);
        BufferedReader fss = new BufferedReader(fs);
        String className = fss.readLine();

        if (className == null) {
            return null;
        }

        Type type = null;
        if(className.contains("ArrayList")) {
            String inner = className.substring("ArrayList<".length(), className.length()-1);
            Class innerClass = null;
            try {
                innerClass = Class.forName(inner);
                type = TypeToken.get(Utils.createListOfType(innerClass).getClass()).getType();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        String line = null;
        String content = "";
        while ((line = fss.readLine()) != null) {
            content += line;
        }

        fss.close();

        Class<?> clazz;
        try {
            clazz = Class.forName(className);

            if(className.contains("google")) {
                return mJsonFactory.createJsonParser(content).parseAndClose(clazz, null);
            } else if(type != null) {
                return mGson.fromJson(content, type);
            } else
                return mGson.fromJson(content, clazz);
        } catch(IllegalArgumentException e) {
            Log.e(LOG_TAG, "Deserializing from disk failed");
            return null;
        } catch(ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    protected void cacheToDisk(String key, Object value) {

        new Builder<Object,Void>(Object.class, Void.class)
                .addParameter(key)
                .addParameter(value)
                .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<Object, Void>() {
                    @Override
                    public Void doInBackground(Object... params) {
                        Log.d(LOG_TAG, "Writing model to disk");
                        File file = new File(diskCacheDirectory + "/" + getFileNameForKey((String) params[0]));
                        try {
                            file.createNewFile();
                            file.deleteOnExit();

                            writeValueToDisk(file, params[1]);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }).buildAndExecute();

    }

    @Override
    protected void writeValueToDisk(File file, Object o) throws IOException {

        if(file.exists())
            file.delete();

        FileWriter fstream = new FileWriter(file, true);
        BufferedWriter out = new BufferedWriter(fstream);

        String className = o.getClass().getCanonicalName();

        if(o instanceof ArrayList) {
            ArrayList d = (ArrayList)o;
            if(d.size() > 0)
                className = className+"<"+d.get(0).getClass().getCanonicalName()+">";
        }

        String json = mGson.toJson(o);
        out.write(className+"\n");
        out.write(json);

        out.close();
    }

}
