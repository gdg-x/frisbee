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

package org.gdg.frisbee.android.about;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.app.App;

import java.util.Collection;

import butterknife.ButterKnife;
import butterknife.Bind;

class ContributorAdapter extends ArrayAdapter<Contributor> {

    private final LayoutInflater mLayoutInflator;

    public ContributorAdapter(Context context) {
        super(context, R.layout.list_contributor_item);
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflator.inflate(R.layout.list_contributor_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        Contributor contributor = getItem(position);

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.contributorName.setText(contributor.getLogin());

        if (!TextUtils.isEmpty(contributor.getAvatarUrl())) {
            App.getInstance().getPicasso()
                    .load(contributor.getAvatarUrl())
                    .into(viewHolder.contributorAvatar);
        }

        return convertView;
    }

    @Override
    public void addAll(Collection<? extends Contributor> contributors) {
        setNotifyOnChange(false);
        for (Contributor c : contributors) {
            add(c);
        }
        notifyDataSetChanged(); // also sets mNotifyOnChange = true;
    }

    static final class ViewHolder {
        @Bind(R.id.contributorName)
        TextView contributorName;
        @Bind(R.id.contributorIcon)
        ImageView contributorAvatar;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
