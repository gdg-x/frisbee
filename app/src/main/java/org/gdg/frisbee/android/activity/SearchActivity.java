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

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;

import timber.log.Timber;

public class SearchActivity extends GdgNavDrawerActivity {

    public static final String ACTION_FOUND = "org.gdg.frisbee.android.FOUND";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        handleIntent(getIntent());
    }

    protected String getTrackedViewName() {
        return "Search";
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        Timber.d(intent.getDataString());
        if (Intent.ACTION_SEARCH.equals(action)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        } else if (ACTION_FOUND.equals(action)) {
            String id = intent.getDataString();
            Intent chapterIntent = new Intent(this, MainActivity.class);
            chapterIntent.putExtra(Const.EXTRA_CHAPTER_ID, id.replace("item/", ""));
            startActivity(chapterIntent);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void doSearch(String query) {
        startSearch(query, false, null, false);
    }
}
