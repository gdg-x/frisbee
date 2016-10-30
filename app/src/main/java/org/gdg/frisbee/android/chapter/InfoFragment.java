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

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

import com.squareup.picasso.Picasso;
import com.tasomaniac.android.widget.DelayedProgressBar;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.PlusApi;
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
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class InfoFragment extends BaseFragment implements OrganizerLoader.Listener {

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
    private OrganizerLoader organizerLoader;
    private ModelCache modelCache;
    private Picasso picasso;
    private PlusApi plusApi;

    public static InfoFragment newInstance(String plusId) {
        InfoFragment fragment = new InfoFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Const.EXTRA_PLUS_ID, plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        modelCache = App.from(context).getModelCache();
        picasso = App.from(context).getPicasso();
        plusApi = App.from(context).getPlusApi();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mInflater = LayoutInflater.from(getActivity());
        setIsLoading(true);

        chapterPlusId = getArguments().getString(Const.EXTRA_PLUS_ID);
        loadChapter(chapterPlusId);
    }

    @Override
    public void onDestroy() {
        if (organizerLoader != null) {
            organizerLoader.setListener(null);
            organizerLoader.cancel(true);
        }
        super.onDestroy();
    }

    private void loadChapter(final String chapterPlusId) {
        modelCache.getAsync(
            ModelCache.KEY_PERSON + chapterPlusId,
            Utils.isOnline(getActivity()),
            new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    updateUIFrom((Person) item);
                }

                @Override
                public void onNotFound(String key) {
                    loadChapterFromNetwork(chapterPlusId);
                }
            });
    }

    void loadChapterFromNetwork(final String chapterPlusId) {
        plusApi.getPerson(chapterPlusId).enqueue(
            new Callback<Person>() {
                @Override
                public void onSuccess(Person chapter) {
                    putChapterInCache(chapterPlusId, chapter);
                    updateUIFrom(chapter);
                }

                @Override
                public void onError() {
                    showError(R.string.server_error);
                    setIsLoading(false);
                }

                @Override
                public void onNetworkFailure(Throwable error) {
                    showError(R.string.offline_alert);
                    setIsLoading(false);
                }

                private void putChapterInCache(String plusId, Person person) {
                    modelCache.putAsync(
                        ModelCache.KEY_PERSON + plusId,
                        person,
                        DateTime.now().plusDays(1),
                        null
                    );
                }
            });
    }

    void updateUIFrom(Person chapter) {
        if (getActivity() != null) {
            updateChapterUIFrom(chapter);
            addOrganizers(chapter);
        }
    }

    private void updateChapterUIFrom(Person chapter) {
        if (mTagline != null) {
            mTagline.setText(chapter.getTagline());
        }
        if (mAbout != null) {
            mAbout.setText(createAboutText(chapter));
            mAbout.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private static CharSequence createAboutText(Person person) {
        String aboutText = person.getAboutMe();
        if (aboutText == null) {
            return SpannedString.valueOf("");
        }
        return createHtmlFormatted(aboutText);
    }

    private void addOrganizers(Person cachedChapter) {
        setIsLoading(false);
        List<Urls> urls = cachedChapter.getUrls();
        if (urls == null) {
            return;
        }
        ArrayList<String> organizerIds = new ArrayList<>();
        for (Urls url : urls) {
            if (isNonCommunityPlusUrl(url)) {
                String org = url.getValue();
                try {
                    organizerIds.add(getGPlusIdFromPersonUrl(url));
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
        organizerLoader = new OrganizerLoader(getContext());
        organizerLoader.setListener(this);
        organizerLoader.execute(organizerIds.toArray(new String[organizerIds.size()]));
    }

    private static boolean isNonCommunityPlusUrl(Urls url) {
        return url.getValue().contains("plus.google.com/") && !url.getValue().contains("communities");
    }

    private void addUrlToUI(Urls url) {
        TextView tv = (TextView) mInflater
            .inflate(R.layout.list_resource_item, (ViewGroup) getView(), false);
        tv.setText(createHtmlFormatted("<a href='" + url.getValue() + "'>" + url.getLabel() + "</a>"));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        mResourcesBox.addView(tv);
    }

    @SuppressWarnings("deprecation")
    private static Spanned createHtmlFormatted(String source) {
        return Html.fromHtml(source);
    }

    @Override
    public void onOrganizerLoaded(final Person organizer) {
        View v = createOrganizerView(organizer);
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

    private View createOrganizerView(Person organizer) {
        View convertView = mInflater.inflate(R.layout.list_organizer_item, (ViewGroup) getView(), false);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);

        if (organizer.getImage() != null) {
            picasso.load(organizer.getImage().getUrl())
                .transform(new BitmapBorderTransformation(0,
                    getResources().getDimensionPixelSize(R.dimen.organizer_icon_size) / 2,
                    ContextCompat.getColor(getContext(), R.color.white)))
                .placeholder(R.drawable.ic_no_avatar)
                .into(picture);
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(organizer.getDisplayName());

        return convertView;
    }

    @Override
    public void onUnknownOrganizerLoaded() {
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
        if (isLoading == mLoading || !isContextValid()) {
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
