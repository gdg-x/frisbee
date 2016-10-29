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

package org.gdg.frisbee.android.provider;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.activity.SearchActivity;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;

import timber.log.Timber;

public class GdgProvider extends ContentProvider {

    private static final String[] CHAPTER_COLUMNS = new String[]{
        BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
    };

    private static final int SEARCH_SUGGEST = 0;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(BuildConfig.APPLICATION_ID + ".provider",
            SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    }

    private Directory mDirectory;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        if (!isSetupDone()) {
            prepareProvider();
        }

        String searchString = uri.getLastPathSegment();
        return getSuggestions(searchString);
    }

    private boolean isSetupDone() {
        return mDirectory != null;
    }

    private void prepareProvider() {
        mDirectory = (Directory) App.from(getContext()).getModelCache().get(ModelCache.KEY_CHAPTER_LIST_HUB);
        Timber.d("Initialized ContentProvider");
    }

    private MatrixCursor getSuggestions(String query) {
        MatrixCursor cursor = new MatrixCursor(CHAPTER_COLUMNS);

        for (Chapter chapter : mDirectory.getGroups()) {
            if (chapter.getName().toLowerCase().contains(query.toLowerCase())) {
                cursor.addRow(new Object[]{
                    chapter.getGplusId().hashCode(),
                    chapter.getName(),
                    chapter.getCity() + ", " + chapter.getCountry().getName(),
                    SearchActivity.ACTION_FOUND,
                    chapter.getGplusId()
                });
            }
        }
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }
}
