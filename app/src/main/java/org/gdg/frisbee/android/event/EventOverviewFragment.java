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

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgActivity;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

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

    @InjectView(R.id.group_logo)
    ImageView mGroupLogo;

    private boolean mLoading;
    private GroupDirectory mClient;
    private Directory mDirectory;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_overview, null);
        ButterKnife.inject(this, v);

        mClient = new GroupDirectory();
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
        mClient.getEvent(getArguments().getString(Const.EXTRA_EVENT_ID), this, this).execute();
    }

    public void updateWithDetails(EventFullDetails eventFullDetails) {
        mTitle.setText(eventFullDetails.getTitle());
        String about = eventFullDetails.getAbout();
        if (about == null) {
            about = getString(R.string.no_description_available);
        }
        mEventDescription.setText(Html.fromHtml(about));
        mStartTime.setText(getString(R.string.starting_at, formatTime(eventFullDetails)));
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
    public void onResponse(final EventFullDetails eventFullDetails) {
        updateWithDetails(eventFullDetails);

        App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                mDirectory = (Directory) item;
                updateGroupDetails(mDirectory.getGroupById(eventFullDetails.getChapter()));
            }

            @Override
            public void onNotFound(String key) {
                if (Utils.isOnline(getActivity())) {
                    mClient.getDirectory(new Response.Listener<Directory>() {
                        @Override
                        public void onResponse(final Directory directory) {
                            mDirectory = directory;
                            updateGroupDetails(mDirectory.getGroupById(eventFullDetails.getChapter()));
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Crouton.makeText(getActivity(), getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                            Timber.e("Could'nt fetch chapter list", volleyError);
                        }
                    }).execute();
                } else {
                    Crouton.makeText(getActivity(), getString(R.string.offline_alert), Style.ALERT).show();
                }
            }
        });
    }

    private void updateGroupDetails(Chapter group) {
        Plus.PeopleApi.load(((GdgActivity)getActivity()).getGoogleApiClient(), group.getGplusId())
                .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(People.LoadPeopleResult loadPeopleResult) {
                        if (loadPeopleResult.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS ) {
                            Person gplusChapter = loadPeopleResult.getPersonBuffer().get(0);
                            if (gplusChapter.getImage().hasUrl()) {
                                Picasso.with(getActivity()).load(gplusChapter.getImage().getUrl()).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                                        BitmapDrawable logo = new BitmapDrawable(getResources(), bitmap );
                                        mGroupLogo.setVisibility(View.VISIBLE);
                                        mGroupLogo.setImageDrawable(logo);
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable drawable) {
                                        mGroupLogo.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable drawable) {

                                    }
                                });
                            }
                        }
                    }
                });
        ((GdgActivity) getActivity()).getSupportActionBar().setTitle(group.getShortName());
        mGroupLogo.setVisibility(View.INVISIBLE);
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
