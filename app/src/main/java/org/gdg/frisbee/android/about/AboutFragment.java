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

package org.gdg.frisbee.android.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.BaseFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AboutFragment extends BaseFragment {

    @Bind(R.id.version)
    TextView mVersion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVersion.setText(BuildConfig.VERSION_NAME);

        if (BuildConfig.DEBUG || BuildConfig.BUILD_TYPE.equals("alpha")) {
            mVersion.append("\nCommit SHA: ");
            mVersion.append(BuildConfig.COMMIT_SHA);
            mVersion.append("\nCommit Time: ");
            mVersion.append(BuildConfig.COMMIT_TIME);
        }
    }
}
