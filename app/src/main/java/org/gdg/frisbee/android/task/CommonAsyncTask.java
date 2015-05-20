/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.task;

import android.os.AsyncTask;

import java.lang.reflect.Array;

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
    private OnPostExecuteListener<Params, Result> mPostListener;
    private OnPreExecuteListener mPreListener;
    private Params[] mParams;

    private final Class<Params> mParamsType;
    private final Class<Result> mResultType;

    public CommonAsyncTask(Class<Params> paramsType, Class<Result> resultType) {
        super();
        mParamsType = paramsType;
        mResultType = resultType;
    }

    public Class<Params> getParamsType() {
        return mParamsType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mPreListener != null) {
            mPreListener.onPreExecute();
        }
    }

    @Override
    protected final Result doInBackground(Params... params) {
        if (params != null && params.length == 0 && mBackgroundListener != null) {
            return mBackgroundListener.doInBackground((Params[]) null);
        }


        mParams = (Params[]) Array.newInstance(mParamsType, params.length);
        for (int i = 0; i < params.length; i++) {
            mParams[i] = params[i];
        }
        if (mBackgroundListener != null) {
            return mBackgroundListener.doInBackground(mParams);
        }

        return null;
    }

    @Override
    protected final void onPostExecute(Result success) {
        super.onPostExecute(success);

        if (mPostListener != null) {
            mPostListener.onPostExecute(mParams, success);
        }
    }

    public void setBackgroundListener(OnBackgroundExecuteListener<Params, Result> mBackgroundListener) {
        this.mBackgroundListener = mBackgroundListener;
    }

    public void setPreListener(OnPreExecuteListener mPreListener) {
        this.mPreListener = mPreListener;
    }

    public void setPostListener(OnPostExecuteListener<Params, Result> mPostListener) {
        this.mPostListener = mPostListener;
    }

    public void setParameters(Params[] mParams) {
        this.mParams = mParams;
    }

    public interface OnBackgroundExecuteListener<Params, Result> {
        Result doInBackground(Params... params);
    }

    public interface OnPostExecuteListener<Params, Result> {
        void onPostExecute(Params[] params, Result result);
    }

    public interface OnPreExecuteListener {
        void onPreExecute();
    }

}
