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

package org.gdg.frisbee.android.task;

import android.os.AsyncTask;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.GenericJson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.task
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 00:06
 */
public class CommonAsyncTask<Params, Result> extends AsyncTask<Params, Void, Result> {

    private OnBackgroundExecuteListener<Params, Result> mBackgroundListener;
    private OnPostExecuteListener<Result> mPostListener;
    private OnPreExecuteListener mPreListener;
    private Params[] mParams;

    private final Class<Params> mParamsType;
    private final Class<Result> mResultType;

    public CommonAsyncTask(Class<Params> paramsType, Class<Result> resultType) {
        super();
        mParamsType = paramsType;
        mResultType = resultType;
    }

    public Class<Result> getResultType() {
        return mResultType;
    }

    public Class<Params> getParamsType() {
        return mParamsType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(mPreListener != null)
            mPreListener.onPreExecute();
    }

    @Override
    protected final Result doInBackground(Params... params) {
        if(params != null && params.length == 0 && mBackgroundListener != null)
            return mBackgroundListener.doInBackground(null);


        ArrayList<Params> p = new ArrayList<Params>();
        if(params != null)
            for(int i = 0; i < params.length; i++) {
                p.add(params[i]);
            }

        Params[] p2 = (Params[]) Array.newInstance(mParamsType,p.size());

        if(mBackgroundListener != null) {
            return mBackgroundListener.doInBackground(p.toArray(p2));
        }

        return null;
    }

    @Override
    protected final void onPostExecute(Result success) {
        super.onPostExecute(success);

        if(mPostListener != null)
            mPostListener.onPostExecute(success);
    }

    public OnBackgroundExecuteListener<Params, Result> getBackgroundListener() {
        return mBackgroundListener;
    }

    public void setBackgroundListener(OnBackgroundExecuteListener<Params, Result> mBackgroundListener) {
        this.mBackgroundListener = mBackgroundListener;
    }

    public OnPreExecuteListener getPreListener() {
        return mPreListener;
    }

    public void setPreListener(OnPreExecuteListener mPreListener) {
        this.mPreListener = mPreListener;
    }

    public OnPostExecuteListener<Result> getPostListener() {
        return mPostListener;
    }

    public void setPostListener(OnPostExecuteListener<Result> mPostListener) {
        this.mPostListener = mPostListener;
    }

    public Params[] getParameters() {
        return mParams;
    }

    public void setParameters(Params[] mParams) {
        this.mParams = mParams;
    }

    public interface OnBackgroundExecuteListener<Params, Result> {
        public Result doInBackground(Params... params);
    }

    public interface OnPostExecuteListener<Result> {
        public void onPostExecute(Result result);
    }

    public interface OnPreExecuteListener {
        public void onPreExecute();
    }

    public CommonAsyncTask<Params, Result> execute() {
        super.execute(mParams);
        return this;
    }
}