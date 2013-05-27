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
import android.widget.TextView;
import com.android.volley.toolbox.NetworkImageView;
import com.google.api.services.plus.model.Person;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.GdgVolley;
import org.gdg.frisbee.android.view.NetworkedCacheableImageView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.adapter
 * <p/>
 * User: maui
 * Date: 26.04.13
 * Time: 08:24
 */
public class OrganizerAdapter extends ArrayAdapter<Person> {

    private LayoutInflater mInflater;

    public OrganizerAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = mInflater.inflate(R.layout.list_organizer_item, null);

        Person item = (Person) getItem(position);

        NetworkImageView picture = (NetworkImageView) convertView.findViewById(R.id.icon);
        picture.setImageUrl(item.getImage().getUrl(), GdgVolley.getInstance().getImageLoader());

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(item.getDisplayName());

        return convertView;
    }
}
