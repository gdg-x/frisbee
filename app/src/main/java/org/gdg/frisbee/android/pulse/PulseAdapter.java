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

package org.gdg.frisbee.android.pulse;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.api.model.PulseEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

class PulseAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final ArrayList<Map.Entry<String, PulseEntry>> mPulse;
    @Nullable
    private final Directory mDirectory;

    private int[] mPositions;
    private int mMode;
    private boolean mCheckValuesAgainstDirectory;

    public PulseAdapter(Context ctx, @Nullable int[] positions, @Nullable Directory directory) {
        mInflater = LayoutInflater.from(ctx);
        mPulse = new ArrayList<>();
        mPositions = positions;
        mDirectory = directory;
    }

    public int[] getPositions() {
        return mPositions;
    }

    @Override
    public int getCount() {
        return mPulse.size();
    }

    @Override
    public Map.Entry<String, PulseEntry> getItem(int i) {
        return mPulse.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.list_pulse_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        Map.Entry<String, PulseEntry> entry = getItem(i);

        if (i > 0) {
            Map.Entry<String, PulseEntry> prevEntry = getItem(i - 1);
            int z = 2;

            while (entry.getValue().compareTo(mMode, prevEntry.getValue()) == 0 && (i - z) >= 0) {
                prevEntry = getItem(i - z);
                z++;
            }
            if (z > 2) {
                mPositions[i] = mPositions[mPulse.indexOf(prevEntry)] + 1;
            } else {
                mPositions[i] = mPositions[i - 1] + 1;

            }
            holder.position.setText(mPositions[i] + ".");

        } else {
            mPositions[i] = 1;
            holder.position.setText("1.");
        }

        holder.key.setText(entry.getKey());

        switch (mMode) {
            case 0:
                holder.value.setText("(" + entry.getValue().getMeetings() + ")");
                break;
            case 1:
                holder.value.setText("(" + entry.getValue().getAttendees() + ")");
                break;
            case 2:
                holder.value.setText("(" + entry.getValue().getPlusMembers() + ")");
                break;
        }

        if (mCheckValuesAgainstDirectory && mDirectory != null) {
            rowView.setEnabled(mDirectory.getGroupById(entry.getValue().getId()) != null);
            holder.key.setEnabled(mDirectory.getGroupById(entry.getValue().getId()) != null);
        }
        return rowView;
    }

    public void setPulse(final int mode, Pulse pulse, boolean checkValuesAgainstDirectory) {
        mMode = mode;
        mCheckValuesAgainstDirectory = checkValuesAgainstDirectory;
        mPulse.clear();
        mPulse.addAll(pulse.entrySet());
        //Only initialize if the positions are not provided.
        //They are provided in constructor and coming from savedInstanceState of the Fragment.
        if (mPositions == null || mPositions.length != mPulse.size()) {
            mPositions = new int[mPulse.size()];
        }

        Collections.sort(mPulse, new Comparator<Map.Entry<String, PulseEntry>>() {
            @Override
            public int compare(Map.Entry<String, PulseEntry> entry, Map.Entry<String, PulseEntry> entry2) {
                return entry.getValue().compareTo(mode, entry2.getValue());
            }
        });
    }

    static class ViewHolder {
        @BindView(R.id.position)
        public TextView position;
        @BindView(R.id.key)
        public TextView key;
        @BindView(R.id.value)
        public TextView value;

        public ViewHolder(View v) {
            ButterKnife.bind(this, v);
        }
    }
}
