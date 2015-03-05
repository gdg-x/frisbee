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

package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.widget.Toast;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.utils.PrefUtils;

public class StartActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG && TextUtils.isEmpty(BuildConfig.PLAY_APP_ID)) {
            Toast.makeText(this, "no API keys defined!", Toast.LENGTH_SHORT).show();
        }

        Intent intentForStart;
        if (PrefUtils.isFirstStart(this)) {
            intentForStart = new Intent(StartActivity.this, FirstStartActivity.class);
        } else {
            intentForStart = new Intent(StartActivity.this, MainActivity.class);

            final Chapter selectedChapter = PrefUtils.getHomeChapter(this);
            if (selectedChapter != null) {
                intentForStart.putExtra(Const.EXTRA_CHAPTER, selectedChapter);
            }
        }

        startActivity(intentForStart);
        finish();
    }

}
