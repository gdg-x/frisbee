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

package org.gdg.frisbee.android.event;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.ButterKnife;
import butterknife.Bind;
import retrofit.Callback;
import retrofit.RetrofitError;
import timber.log.Timber;

public class EventOverviewFragment extends Fragment {

    @Bind(R.id.title)
    TextView mTitle;

    @Bind(R.id.date)
    TextView mDate;

    @Bind(R.id.start_time)
    TextView mStartTime;

    @Bind(R.id.event_description)
    TextView mEventDescription;

    @Bind(R.id.loading)
    View mProgressContainer;

    @Bind(R.id.group_logo)
    ImageView mGroupLogo;

    private boolean mLoading;
    private Directory mDirectory;
    private EventFullDetails mEvent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event_overview, parent, false);
        ButterKnife.bind(this, v);

        setHasOptionsMenu(true);
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
        final String eventId = getArguments().getString(Const.EXTRA_EVENT_ID);
        App.getInstance().getGdgXHub().getEventDetail(eventId, new Callback<EventFullDetails>() {
            @Override
            public void success(EventFullDetails eventFullDetails, retrofit.client.Response response) {
                onResponse(eventFullDetails);
            }

            @Override
            public void failure(RetrofitError error) {
                if (isAdded()) {
                    Snackbar snackbar = Snackbar.make(getView(), R.string.server_error,
                            Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.alert(snackbar).show();
                }
                Timber.d(error, "error while retrieving event %s", eventId);
            }
        });
    }

    public void updateWithDetails(final EventFullDetails eventFullDetails) {
        mTitle.setText(eventFullDetails.getTitle());

        final String eventUrl = eventFullDetails.getEventUrl();
        if (eventUrl != null && !eventUrl.equals(getActivity().getIntent().getDataString())) {
            mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    launchUrl(eventUrl);
                }
            });
        }

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

    public void onResponse(final EventFullDetails eventFullDetails) {
        if (getActivity() == null) {
            return;
        }
        mEvent = eventFullDetails;

        getActivity().supportInvalidateOptionsMenu();
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
                    App.getInstance().getGdgXHub().getDirectory(new Callback<Directory>() {
                        @Override
                        public void success(Directory directory, retrofit.client.Response response) {

                            mDirectory = directory;
                            updateGroupDetails(mDirectory.getGroupById(eventFullDetails.getChapter()));
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            if (isAdded()) {
                                Snackbar snackbar = Snackbar.make(getView(), R.string.fetch_chapters_failed,
                                        Snackbar.LENGTH_SHORT);
                                ColoredSnackBar.alert(snackbar).show();
                            }
                            Timber.e(error, "Could'nt fetch chapter list");
                        }
                    });
                } else {
                    Snackbar snackbar = Snackbar.make(getView(), R.string.offline_alert,
                            Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.alert(snackbar).show();
                }
            }
        });
    }

    private void updateGroupDetails(Chapter group) {
        if (getActivity() == null) {
            return;
        }

        Plus.PeopleApi.load(((GdgActivity) getActivity()).getGoogleApiClient(), group.getGplusId())
                .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                    @Override
                    public void onResult(People.LoadPeopleResult loadPeopleResult) {
                        if (loadPeopleResult.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                            Person gplusChapter = loadPeopleResult.getPersonBuffer().get(0);
                            if (gplusChapter.getImage().hasUrl()) {
                                Picasso.with(getActivity()).load(gplusChapter.getImage().getUrl()).into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                                        if (!isAdded()) {
                                            return;
                                        }
                                        BitmapDrawable logo = new BitmapDrawable(getResources(), bitmap);
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
        ((GdgActivity) getActivity()).setToolbarTitle(group.getShortName());
        //mGroupLogo.setVisibility(View.INVISIBLE);  //commented as it's making group logo invisible without any condition
    }

    public void setIsLoading(boolean isLoading) {

        if (isLoading == mLoading || getActivity() == null) {
            return;
        }

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
                    if (mProgressContainer != null) {
                        mProgressContainer.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mProgressContainer.startAnimation(fadeOut);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mEvent != null) {
            inflater.inflate(R.menu.event_menu, menu);
            MenuItem shareMenuitem = menu.findItem(R.id.share);

            if (mEvent.getEventUrl() != null) {
                ShareActionProvider mShareActionProvider = 
                        (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuitem);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, mEvent.getEventUrl());
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(shareIntent);
                }
            } else {
                shareMenuitem.setVisible(false);
                menu.findItem(R.id.view_event_url).setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_calendar:
                addEventToCalendar();
                return true;
            case R.id.navigate_to:
                launchNavigation();
                return true;
            case R.id.view_event_url:
                launchUrl(mEvent.getEventUrl());
                return true;
            default:
                return false;
        }
    }

    private void launchNavigation() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + mEvent.getLocation()));
        if (Utils.canLaunch(getActivity(), intent)) {
            startActivity(intent);
        }
    }

    private void launchUrl(String eventUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(eventUrl));
        startActivity(intent);
    }

    public void addEventToCalendar() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");

        intent.putExtra("beginTime", mEvent.getStart().getMillis());
        intent.putExtra("endTime", mEvent.getEnd().getMillis());
        intent.putExtra("title", mEvent.getTitle());

        String location = mEvent.getLocation();
        if (location != null) {
            intent.putExtra("eventLocation", location);
        }

        startActivity(intent);
    }

    public static Fragment createfor(String eventId) {
        EventOverviewFragment fragment = new EventOverviewFragment();
        Bundle args = new Bundle();
        args.putString(Const.EXTRA_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }
}
