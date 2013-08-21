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

package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.FirstStartActivity;
import roboguice.inject.InjectView;

public class FirstStartStep3Fragment extends RoboSherlockFragment {

    private static String LOG_TAG = "GDG-FirstStartStep3Fragment";

    @InjectView(R.id.complete)
    Button mCompleteButton;

    @InjectView(R.id.enable_gcm)
    CheckBox mEnableGcm;

    @InjectView(R.id.enable_analytics)
    CheckBox mEnableAnalytics;

    public static FirstStartStep3Fragment newInstance() {
        FirstStartStep3Fragment fragment = new FirstStartStep3Fragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCompleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(getActivity() instanceof Step3Listener) {
                    ((Step3Listener)getActivity()).onComplete(mEnableAnalytics.isChecked(), mEnableGcm.isChecked());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_step3, null);
    }

    public interface Step3Listener {
        void onComplete(boolean enableAnalytics, boolean enableGcm);
    }
}
