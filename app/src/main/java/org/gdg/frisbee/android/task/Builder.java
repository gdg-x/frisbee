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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.task
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 00:32
 */
public class Builder<Params, Result> {

    private CommonAsyncTask<Params, Result> mTask;
    private ArrayList<Params> mParams;

    public Builder(Class<Params> paramsType, Class<Result> resultType) {
        mTask = new CommonAsyncTask<Params, Result>(paramsType, resultType);
        mParams = new ArrayList<Params>();
    }

    public Builder<Params, Result> addParameter(Params param) {
        mParams.add(param);
        return this;
    }

    public Builder<Params, Result> setParameter(ArrayList<Params> params) {
        mParams = params;
        return this;
    }

    public Builder<Params, Result> setOnPostExecuteListener(CommonAsyncTask.OnPostExecuteListener<Params, Result> listener) {
        mTask.setPostListener(listener);
        return this;
    }

    public Builder<Params, Result> setOnBackgroundExecuteListener(CommonAsyncTask.OnBackgroundExecuteListener<Params, Result> listener) {
        mTask.setBackgroundListener(listener);
        return this;
    }

    public Builder<Params, Result> setOnPreExecuteListener(CommonAsyncTask.OnPreExecuteListener listener) {
        mTask.setPreListener(listener);
        return this;
    }

    public CommonAsyncTask<Params, Result> build() {
        Params[] p = (Params[]) Array.newInstance(mTask.getParamsType(), 0);
        mTask.setParameters((Params[])mParams.toArray(p));
        return mTask;
    }

    public void buildAndExecute() {
        build().execute((Params[])mParams.toArray());
    }

    public void buildAndExecuteOnExecutor(Executor executor) {
        build().executeOnExecutor(executor, (Params[])mParams.toArray());
    }
}