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

package org.gdg.frisbee.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.utils.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.adapter
 * <p/>
 * User: maui
 * Date: 24.04.13
 * Time: 00:39
 */
public class ChapterAdapter extends ArrayAdapter<Chapter> {

    private LayoutInflater mInflater;

    public ChapterAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = LayoutInflater.from(context);
    }

    public ChapterAdapter(Context context, int textViewResourceId, List<Chapter> objects) {
        super(context, textViewResourceId, objects);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void addAll(Collection<? extends Chapter> chapters) {
        for(Chapter c : chapters) {
            add(c);
        }
    }

    public ArrayList<Chapter> getAll() {
        ArrayList<Chapter> chapters = new ArrayList<Chapter>();
        for(int i = 0; i < getCount(); i++) {
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
        if(position > 0 && position < getCount())
            return Utils.stringToLong(getItem(position).getGplusId());
        else
            return 0;
    }
}
