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
import android.support.annotation.Nullable;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.activity.SearchActivity;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;

import timber.log.Timber;

public class GdgProvider extends ContentProvider {

    public static final String AUTHORITY = "org.gdg.frisbee.android.provider.GdgProvider";

    private static final String[] CHAPTER_COLUMNS = new String[] {
        BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
        SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
    };

    private static final int SEARCH_SUGGEST = 0;

    private static UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
    }

    @Nullable
    private Directory mDirectory;

    @Override
    public boolean onCreate() {
        return true;
    }

    @NonNull
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        if (mDirectory == null) {
            mDirectory = (Directory) App.getInstance().getModelCache().get(Const.CACHE_KEY_CHAPTER_LIST_HUB);
            Timber.d("Initialized ContentProvider");
        }

        String searchString = uri.getLastPathSegment();
        return getSuggestions(searchString);
    }

    @NonNull
    private MatrixCursor getSuggestions(@NonNull String query) {
        MatrixCursor cursor = new MatrixCursor(CHAPTER_COLUMNS);

        if (mDirectory != null) {
            for (Chapter chapter : mDirectory.getGroups()) {
                if (chapter.getName().toLowerCase().contains(query.toLowerCase())) {
                    cursor.addRow(new String[]{
                        chapter.getGplusId(),
                        chapter.getName(),
                        chapter.getCity() + ", " + chapter.getCountry(),
                        SearchActivity.ACTION_FOUND,
                        chapter.getGplusId()
                    });
                }
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

    @NonNull
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new UnsupportedOperationException();
    }
}
