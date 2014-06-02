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

package org.gdg.frisbee.android.event;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class EventOverviewFragment extends Fragment implements Response.Listener<EventFullDetails>, Response.ErrorListener {

    @InjectView(R.id.title)
    TextView mTitle;

    @InjectView(R.id.date)
    TextView mDate;

    @InjectView(R.id.start_time)
    TextView mStartTime;

    @InjectView(R.id.event_description)
    TextView mEventDescription;

    @InjectView(R.id.loading)
    View mProgressContainer;

    private boolean mLoading;
    private GroupDirectory mClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_overview, null);
        ButterKnife.inject(this, v);

        mClient = new GroupDirectory();
        mClient.getEvent(getArguments().getString(Const.EXTRA_EVENT_ID), this, this).execute();

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setIsLoading(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void updateWithDetails(EventFullDetails eventFullDetails) {
        mTitle.setText(eventFullDetails.getTitle());
        mEventDescription.setText(Html.fromHtml(eventFullDetails.getAbout()));
        mStartTime.setText(formatTime(eventFullDetails));
        mDate.setText(formatDate(eventFullDetails));
        setIsLoading(false);
    }

    private String formatTime(EventFullDetails eventFullDetails) {
        DateTimeFormatter fmt = DateTimeFormat.shortTime();
        return fmt.print(eventFullDetails.getStart());
    }

    private String formatDate(EventFullDetails eventFullDetails) {

        DateTimeFormatter fmt = DateTimeFormat.fullDate();
        // TODO check whether this is a multi day event
        return fmt.print(eventFullDetails.getStart());
    }

    @Override
    public void onResponse(EventFullDetails eventFullDetails) {
        updateWithDetails(eventFullDetails);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Crouton.makeText(getActivity(), R.string.server_error, Style.ALERT).show();
    }

    public void setIsLoading(boolean isLoading) {

        if (isLoading == mLoading || getActivity() == null)
            return;

        mLoading = isLoading;

        if (isLoading) {
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), android.R.anim.fade_in));
            mProgressContainer.setVisibility(View.VISIBLE);
        } else {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mProgressContainer != null)
                        mProgressContainer.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mProgressContainer.startAnimation(fadeOut);
        }
    }


    public static Fragment createFor(String eventId) {
        EventOverviewFragment fragment = new EventOverviewFragment();
        Bundle args = new Bundle();
        args.putString(Const.EXTRA_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }
}
