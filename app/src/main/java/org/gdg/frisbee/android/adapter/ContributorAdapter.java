/*
 * Copyright 2013, 2015 The GDG Frisbee Project
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
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Collection;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Contributor;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ContributorAdapter extends ArrayAdapter<Contributor> {

    public ContributorAdapter(Context context) {
        super(context, R.layout.contributor_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.contributor_item, null);
            ViewHolder viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        Contributor contributor = getItem(position);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.contributorName.setText(contributor.getLogin());

        if (!TextUtils.isEmpty(contributor.getAvatarUrl())) {
            Picasso.with(getContext()).load(contributor.getAvatarUrl()).into(viewHolder.contributorAvatar);
        }

        return convertView;
    }

    @Override
    public void addAll(Collection<? extends Contributor> contributors) {
        setNotifyOnChange(false);
        for (Contributor c : contributors) {
            add(c);
        }
        setNotifyOnChange(true);
        notifyDataSetChanged();
    }

    private static final class ViewHolder {
        @InjectView(R.id.contributorName)
        TextView contributorName;
        @InjectView(R.id.contributorIcon)
        ImageView contributorAvatar;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
