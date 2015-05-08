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

package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.gdg.frisbee.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FirstStartStep3Fragment extends BaseFragment {

    @InjectView(R.id.complete)
    Button mCompleteButton;

    @InjectView(R.id.gcmContainer)
    LinearLayout mGcmContainer;

    @InjectView(R.id.enable_gcm)
    CheckBox mEnableGcm;

    @InjectView(R.id.enable_analytics)
    CheckBox mEnableAnalytics;

    private boolean mIsSignedIn = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCompleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (getActivity() instanceof Step3Listener) {
                    ((Step3Listener) getActivity()).onComplete(mEnableAnalytics.isChecked(), mEnableGcm.isChecked());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mIsSignedIn) {
            mGcmContainer.setVisibility(View.GONE);
        } else {
            mGcmContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome_step3, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    public void setSignedIn(boolean mIsSignedIn) {
        this.mIsSignedIn = mIsSignedIn;

        if (mGcmContainer != null) {
            if (!mIsSignedIn) {
                mGcmContainer.setVisibility(View.GONE);
            } else {
                mGcmContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface Step3Listener {
        void onComplete(boolean enableAnalytics, boolean enableGcm);
    }
}
