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

package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.activity
 * <p/>
 * User: maui
 * Date: 25.06.13
 * Time: 21:15
 */
public class SettingsActivity extends GdgActivity  {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setContentView(R.layout.activity_settings);

        App.getInstance().getTracker().sendView("/Settings");



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);

    }

}
