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
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.google.android.gms.plus.PlusOneButton;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Pulse;
import org.gdg.frisbee.android.api.model.PulseEntry;
import org.gdg.frisbee.android.view.ResizableImageView;

import java.util.*;

public class PulseAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Map.Entry<String, PulseEntry>> mPulse;
    private int[] mPosition;

    private int mMode;

    public PulseAdapter(Context ctx) {
        mContext = ctx;
        mInflater = LayoutInflater.from(mContext);
        mPulse = new ArrayList<Map.Entry<String, PulseEntry>>();
    }

    @Override
    public int getCount() {
        return mPulse.size();
    }

    @Override
    public Object getItem(int i) {
        return mPulse.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.list_pulse_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.position = (TextView) rowView.findViewById(R.id.position);
            viewHolder.key = (TextView) rowView.findViewById(R.id.key);
            viewHolder.value = (TextView) rowView.findViewById(R.id.value);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        Map.Entry<String, PulseEntry> entry = (Map.Entry<String, PulseEntry>) getItem(i);

        if(i > 0) {
            Map.Entry<String, PulseEntry> prevEntry = (Map.Entry<String, PulseEntry>) getItem(i-1);
            int z = 2;

            while(entry.getValue().compareTo(mMode, prevEntry.getValue()) == 0 && (i-z) >= 0) {
                prevEntry = (Map.Entry<String, PulseEntry>) getItem(i-z);
                z++;
            }
            if(z > 2) {
                mPosition[i] = mPosition[mPulse.indexOf(prevEntry)]+1;
            } else {
                mPosition[i] = (mPosition[i-1]+1);

            }
            holder.position.setText(mPosition[i]+".");

        } else {
            mPosition[i] = 1;
            holder.position.setText("1.");
        }

        holder.key.setText(entry.getKey());

        switch(mMode) {
            case 0:
                holder.value.setText("("+entry.getValue().getMeetings()+")");
                break;
            case 1:
                holder.value.setText("("+entry.getValue().getAttendees()+")");
                break;
            case 2:
                holder.value.setText("("+entry.getValue().getPlusMembers()+")");
                break;
        }

        return rowView;
    }

    public void setPulse(final int mode, Pulse pulse) {
        mMode = mode;
        mPulse.clear();
        mPulse.addAll(pulse.entrySet());
        mPosition = new int[mPulse.size()];

        Collections.sort(mPulse, new Comparator<Map.Entry<String, PulseEntry>>() {
            @Override
            public int compare(Map.Entry<String, PulseEntry> entry, Map.Entry<String, PulseEntry> entry2) {
                PulseEntry value = entry.getValue();
                PulseEntry value2 = entry2.getValue();

                switch(mode) {
                    case 0:
                        return (value.getMeetings()-value2.getMeetings())*-1;
                    case 1:
                        return (value.getAttendees()-value2.getAttendees())*-1;
                    case 2:
                        return (value.getPlusMembers()-value2.getPlusMembers())*-1;
                }
                return 0;
            }
        });
    }

    static class ViewHolder {
        public TextView position, key, value;
    }
}
