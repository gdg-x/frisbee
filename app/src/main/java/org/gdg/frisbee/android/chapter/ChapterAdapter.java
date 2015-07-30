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

package org.gdg.frisbee.android.chapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;

public class ChapterAdapter extends ArrayAdapter<Chapter> {

    public ChapterAdapter(Context context, @LayoutRes int layoutRes) {
        super(context, layoutRes);
    }

    @Override
    public void addAll(@NonNull Collection<? extends Chapter> chapters) {
        for (Chapter c : chapters) {
            add(c);
        }
    }

    public ArrayList<Chapter> getAll() {
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            chapters.add(getItem(i));
        }
        return chapters;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        if (position > 0 && position < getCount()) {
            return Utils.stringToLong(getItem(position).getGplusId());
        } else {
            return 0;
        }
    }

    @Nullable
    public Chapter findById(final String chapterId) {
        int position = getPosition(chapterId);
        if (position != -1) {
            return getItem(position);
        }
        return null;
    }

    public int getPosition(String chapterId) {
        return super.getPosition(new Chapter(null, chapterId));
    }
}
