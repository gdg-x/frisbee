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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.tasomaniac.android.widget.DelayedProgressBar;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.api.model.plus.ImageInfo;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.BaseFragment;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.BindView;

public class EventOverviewFragment extends BaseFragment {

    private static final int REQUEST_INVITE = 1;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.date)
    TextView mDate;

    @BindView(R.id.start_time)
    TextView mStartTime;

    @BindView(R.id.event_description)
    TextView mEventDescription;

    @BindView(R.id.loading)
    DelayedProgressBar mProgressContainer;

    @BindView(R.id.group_logo)
    ImageView mGroupLogo;

    @BindView(R.id.container)
    View mContainer;

    private boolean mLoading;
    private Directory mDirectory;
    private EventFullDetails mEvent;
    @Nullable
    private ImageInfo chapterImageOfEvent;

    public static Fragment createfor(String eventId) {
        EventOverviewFragment fragment = new EventOverviewFragment();
        Bundle args = new Bundle();
        args.putString(Const.EXTRA_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflateView(inflater, R.layout.fragment_event_overview, parent);
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
        App.getInstance().getGdgXHub().getEventDetail(eventId).enqueue(new Callback<EventFullDetails>() {
            @Override
            public void success(EventFullDetails eventFullDetails) {
                onSuccess(eventFullDetails);
            }

            @Override
            public void failure(Throwable error) {
                showError(R.string.server_error);
            }

            @Override
            public void networkFailure(Throwable error) {
                showError(R.string.offline_alert);
            }
        });
    }

    private void updateWithDetails(final EventFullDetails eventFullDetails) {
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

    private void onSuccess(final EventFullDetails eventFullDetails) {
        if (getActivity() == null) {
            return;
        }
        if (getActivity() instanceof Callbacks) {
            ((Callbacks) getActivity()).onEventLoaded(eventFullDetails);
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
                    App.getInstance().getGdgXHub().getDirectory().enqueue(new Callback<Directory>() {
                        @Override
                        public void success(Directory directory) {
                            mDirectory = directory;
                            updateGroupDetails(mDirectory.getGroupById(eventFullDetails.getChapter()));
                        }

                        @Override
                        public void failure(Throwable error) {
                            showError(R.string.fetch_chapters_failed);
                        }

                        @Override
                        public void networkFailure(Throwable error) {
                            showError(R.string.offline_alert);
                        }
                    });
                } else {
                    showError(R.string.offline_alert);
                }
            }
        });
    }

    private void updateGroupDetails(Chapter group) {
        if (getActivity() == null) {
            return;
        }
        loadChapterImage(group.getGplusId());

        ((GdgActivity) getActivity()).setToolbarTitle(group.getShortName());
    }

    private void loadChapterImage(String gplusId) {
        App.getInstance().getPlusApi().getImageInfo(gplusId).enqueue(new Callback<ImageInfo>() {
            @Override
            public void success(ImageInfo imageInfo) {
                chapterImageOfEvent = imageInfo;
                if (isContextValid()) {
                    String imageUrl = imageInfo.getImage().getUrl().replace("sz=50", "sz=196");
                    App.getInstance().getPicasso().load(imageUrl).into(mGroupLogo);
                }
            }

            @Override
            public void failure(Throwable error) {
                if (isContextValid()) {
                    mGroupLogo.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void networkFailure(Throwable error) {
                if (isContextValid()) {
                    mGroupLogo.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void setIsLoading(boolean isLoading) {

        if (isLoading == mLoading || getActivity() == null) {
            return;
        }

        mLoading = isLoading;

        if (isLoading) {
            mContainer.setVisibility(View.GONE);
            mProgressContainer.show(true);
        } else {
            mProgressContainer.hide(true, new Runnable() {
                @Override
                public void run() {
                    if (mContainer != null) {
                        mContainer.setAlpha(0.0f);
                        mContainer.setVisibility(View.VISIBLE);
                        mContainer.animate().alpha(1.0f);
                    }
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mEvent != null) {
            inflater.inflate(R.menu.event_menu, menu);
            MenuItem shareMenuItem = menu.findItem(R.id.share);

            if (mEvent.getEventUrl() != null) {
                ShareActionProvider mShareActionProvider =
                    (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, mEvent.getEventUrl());
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(shareIntent);
                }
            } else {
                shareMenuItem.setVisible(false);
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
            case R.id.share_details:
                shareUrl(mEvent.getEventUrl());
                return true;
            default:
                return false;
        }
    }

    private void shareUrl(String eventUrl) {
        String imageUrl = getString(R.string.invitation_custom_image);
        if (chapterImageOfEvent != null) {
            imageUrl = chapterImageOfEvent.getUrl();
        }
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
            .setMessage(getString(R.string.invitation_message))
            .setDeepLink(Uri.parse(eventUrl))
            //.setCustomImage(Uri.parse(imageUrl))
            .setCallToActionText(getString(R.string.view_event))
            .build();
        startActivityForResult(intent, REQUEST_INVITE);

    }

    private void launchNavigation() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("geo:0,0?q=" + mEvent.getLocation()));
        if (Utils.canLaunch(getActivity(), intent)) {
            startActivity(intent);
        }
    }

    private void launchUrl(String eventUrl) {
        new CustomTabsIntent.Builder()
            .setToolbarColor(getResources().getColor(R.color.theme_primary))
            .setShowTitle(true)
            .build()
            .launchUrl(getActivity(), Uri.parse(eventUrl));
    }

    private void addEventToCalendar() {
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

    public interface Callbacks {
        void onEventLoaded(EventFullDetails eventFullDetails);
    }
}
