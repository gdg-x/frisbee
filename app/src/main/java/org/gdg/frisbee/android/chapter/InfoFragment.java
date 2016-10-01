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

package org.gdg.frisbee.android.chapter;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tasomaniac.android.widget.DelayedProgressBar;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.plus.Person;
import org.gdg.frisbee.android.api.model.plus.Urls;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.BaseFragment;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.BitmapBorderTransformation;
import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class InfoFragment extends BaseFragment {

    @BindView(R.id.about)
    TextView mAbout;

    @BindView(R.id.tagline)
    TextView mTagline;

    @BindView(R.id.organizer_box)
    LinearLayout mOrganizerBox;

    @BindView(R.id.resources_box)
    LinearLayout mResourcesBox;

    @BindView(R.id.loading)
    DelayedProgressBar mProgressContainer;

    @BindView(R.id.container)
    ScrollView mContainer;

    private boolean mLoading = false;

    private LayoutInflater mInflater;
    private String chapterPlusId;


    public static InfoFragment newInstance(String plusId) {
        InfoFragment fragment = new InfoFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_PLUS_ID, plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mInflater = LayoutInflater.from(getActivity());
        setIsLoading(true);

        chapterPlusId = getArguments().getString(Const.EXTRA_PLUS_ID);

        final boolean online = Utils.isOnline(getActivity());
        App.getInstance().getModelCache().getAsync(ModelCache.KEY_PERSON + chapterPlusId,
            online, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    updateUIFrom((Person) item, online);
                }

                @Override
                public void onNotFound(String key) {
                    if (online) {
                        App.getInstance().getPlusApi().getPerson(chapterPlusId).enqueue(
                            new Callback<Person>() {
                                @Override
                                public void success(Person chapter) {
                                    putPersonInCache(chapterPlusId, chapter);
                                    updateUIFrom(chapter, true);
                                }

                                @Override
                                public void failure(Throwable error) {
                                    setIsLoading(false);
                                }

                                @Override
                                public void networkFailure(Throwable error) {
                                    setIsLoading(false);
                                }
                            });
                    } else {
                        showError(R.string.offline_alert);
                        setIsLoading(false);
                    }
                }
            });
    }

    void updateUIFrom(Person chapter, boolean online) {
        if (getActivity() != null) {
            updateChapterUIFrom(chapter);
            addOrganizers(chapter, online);
        }
    }

    private void updateChapterUIFrom(Person chapter) {
        if (mTagline != null) {
            mTagline.setText(chapter.getTagline());
        }
        if (mAbout != null) {
            mAbout.setText(getAboutText(chapter));
            mAbout.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private Spanned getAboutText(Person person) {
        String aboutText = person.getAboutMe();
        if (aboutText == null) {
            return SpannedString.valueOf("");
        }
        return Html.fromHtml(aboutText);
    }

    private void addOrganizers(Person cachedChapter, boolean online) {
        List<Urls> urls = cachedChapter.getUrls();
        if (urls != null) {
            for (int chapterIndex = 0, size = urls.size(); chapterIndex < size; chapterIndex++) {
                Urls url = urls.get(chapterIndex);
                if (isNonCommunityPlusUrl(url)) {
                    String org = url.getValue();
                    try {
                        String id = getGPlusIdFromPersonUrl(url);
                        addOrganizerAsync(id, online);
                    } catch (Exception ex) {
                        if (isContextValid()) {
                            addUrlToUI(url);
                            Timber.w(ex, "Could not parse organizer: %s", org);
                        }
                    }
                } else {
                    addUrlToUI(url);
                }
            }
        }
        setIsLoading(false);
    }

    private boolean isNonCommunityPlusUrl(Urls url) {
        return url.getValue().contains("plus.google.com/") && !url.getValue().contains("communities");
    }

    private void addUrlToUI(Urls url) {
        TextView tv = (TextView) mInflater
            .inflate(R.layout.list_resource_item, (ViewGroup) getView(), false);
        tv.setText(Html.fromHtml("<a href='" + url.getValue() + "'>" + url.getLabel() + "</a>"));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        mResourcesBox.addView(tv);
    }

    private void addOrganizerAsync(final String gplusId, final boolean online) {
        App.getInstance().getModelCache().getAsync(
            ModelCache.KEY_PERSON + gplusId,
            online,
            new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    addOrganizerToUI((Person) item);
                }

                @Override
                public void onNotFound(String key) {
                    if (online) {
                        App.getInstance().getPlusApi().getPerson(gplusId).enqueue(new Callback<Person>() {
                            @Override
                            public void success(Person organizer) {
                                putPersonInCache(gplusId, organizer);
                                addOrganizerToUI(organizer);
                            }

                            @Override
                            public void failure(Throwable error) {
                                addUnknownOrganizerToUI();
                            }

                            @Override
                            public void networkFailure(Throwable error) {
                                addUnknownOrganizerToUI();
                            }
                        });

                    } else {
                        addUnknownOrganizerToUI();
                    }
                }
            }
        );
    }

    private void addOrganizerToUI(final Person organizer) {
        if (organizer == null) {
            addUnknownOrganizerToUI();
            return;
        }

        View v = createOrganizerView(organizer);
        if (v != null) {
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = organizer.getUrl();
                    if (!TextUtils.isEmpty(url)) {
                        startActivity(Utils.createExternalIntent(getActivity(), Uri.parse(url)));
                    }
                }
            });
            if (mOrganizerBox != null) {
                mOrganizerBox.addView(v);
            }
        }
    }

    @Nullable
    private View createOrganizerView(Person person) {
        if (!isContextValid()) {
            return null;
        }
        View convertView = mInflater.inflate(R.layout.list_organizer_item, (ViewGroup) getView(), false);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);

        if (person.getImage() != null) {
            App.getInstance().getPicasso()
                .load(person.getImage().getUrl())
                .transform(new BitmapBorderTransformation(0,
                    getResources().getDimensionPixelSize(R.dimen.organizer_icon_size) / 2,
                    ContextCompat.getColor(getContext(), R.color.white)))
                .placeholder(R.drawable.ic_no_avatar)
                .into(picture);
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(person.getDisplayName());

        return convertView;
    }

    private void addUnknownOrganizerToUI() {
        Timber.d("null person");
        View v = getUnknownOrganizerView();
        if (mOrganizerBox != null) {
            mOrganizerBox.addView(v);
        }
    }

    private View getUnknownOrganizerView() {
        View convertView = mInflater.inflate(R.layout.list_organizer_item, (ViewGroup) getView(), false);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);
        picture.setImageResource(R.drawable.ic_no_avatar);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(R.string.name_not_known);
        return convertView;
    }

    private void putPersonInCache(String plusId, Person person) {
        App.getInstance().getModelCache().putAsync(
            ModelCache.KEY_PERSON + plusId,
            person,
            DateTime.now().plusDays(1),
            null
        );
    }

    private String getGPlusIdFromPersonUrl(Urls personUrl) {
        if (personUrl.getValue().contains("+")) {
            try {
                return "+" + URLDecoder.decode(personUrl.getValue()
                    .replace("plus.google.com/", "")
                    .replace("posts", "")
                    .replace("/", "")
                    .replace("about", "")
                    .replace("u1", "")
                    .replace("u0", "")
                    .replace("https:", "")
                    .replace("http:", "")
                    .replace(chapterPlusId, ""), "UTF-8").trim();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return personUrl.getValue();
            }
        } else {
            return personUrl.getValue()
                .replace("plus.google.com/", "")
                .replace("posts", "")
                .replace("/", "")
                .replace("about", "")
                .replace("u1", "")
                .replace("u0", "")
                .replace("https:", "")
                .replace("http:", "")
                .replace(chapterPlusId, "").replaceAll("[^\\d.]", "").substring(0, 21);
        }
    }

    void setIsLoading(boolean isLoading) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, R.layout.fragment_chapter_info, container);
    }
}
