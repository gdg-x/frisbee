/*
 * Copyright 2013-2015 The GDG Frisbee Project
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
import android.widget.TextView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;

public class ChapterAdapter extends ArrayAdapter<Chapter> {

    private final boolean mWhiteText;
    private LayoutInflater mInflater;

    public ChapterAdapter(Context context, boolean whiteText) {
        super(context, android.R.layout.simple_list_item_1);
        mInflater = LayoutInflater.from(context);
        mWhiteText = whiteText;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        if (!mWhiteText) {
            return super.getView(position, convertView, parent);
        } else {
            View view = convertView;
            if (view == null) {
                view = mInflater.inflate(R.layout.spinner_item_actionbar, parent, false);
            }
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position).getShortName());
            return view;
        }
    }


    @Override
    public void addAll(Collection<? extends Chapter> chapters) {
        for (Chapter c : chapters) {
            add(c);
        }
    }

    public ArrayList<Chapter> getAll() {
        ArrayList<Chapter> chapters = new ArrayList<Chapter>();
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
        if (position > 0 && position < getCount())
            return Utils.stringToLong(getItem(position).getGplusId());
        else
            return 0;
    }

    public Chapter findById(final String chapterId) {
        for (int i = 0; i < getCount(); i++) {
            Chapter chapter = getItem(i);
            if (chapter.getGplusId().equals(chapterId)) {
                return chapter;
            }
        }
        return null;
    }
}
