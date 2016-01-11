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

package org.gdg.frisbee.android.onboarding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.utils.PrefUtils;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG && TextUtils.isEmpty(BuildConfig.IP_SIMPLE_API_ACCESS_KEY)) {
            Toast.makeText(this, "No API keys defined!\nPlease check Github project Wiki page for more detail.", Toast.LENGTH_LONG).show();
        }

        Intent intentForStart;
        if (PrefUtils.isFirstStart(this)) {
            intentForStart = new Intent(StartActivity.this, FirstStartActivity.class);
        } else {
            intentForStart = new Intent(StartActivity.this, MainActivity.class);
        }
        if (getIntent() != null && getIntent().getExtras() != null) {
            intentForStart.putExtras(getIntent().getExtras());
        }
        startActivity(intentForStart);
        finish();
    }
}
