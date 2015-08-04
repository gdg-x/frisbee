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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.api.services.plus.model.Person;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.BaseFragment;
import org.gdg.frisbee.android.common.GdgNavDrawerActivity;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.BitmapBorderTransformation;
import org.gdg.frisbee.android.view.ColoredSnackBar;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class InfoFragment extends BaseFragment {

    @Bind(R.id.about)
    TextView mAbout;

    @Bind(R.id.tagline)
    TextView mTagline;

    @Bind(R.id.organizer_box)
    LinearLayout mOrganizerBox;

    @Bind(R.id.resources_box)
    LinearLayout mResourcesBox;

    @Bind(R.id.loading)
    LinearLayout mProgressContainer;

    @Bind(R.id.container)
    ScrollView mContainer;

    private boolean mLoading = true;

    private LayoutInflater mInflater;

    @NonNull
    private Builder<String, Person[]> mFetchOrganizerInfo = new Builder<>(String.class, Person[].class)
            .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, Person[]>() {
                @Nullable
                @Override
                public Person[] doInBackground(@Nullable String... params) {
                    if (params == null) {
                        return null;
                    }

                    Person[] people = new Person[params.length];
                    for (int i = 0; i < params.length; i++) {
                        Timber.d("Get Organizer " + params[i]);
                        if (isAdded()) {
                            people[i] = GdgNavDrawerActivity.getPersonSync(params[i]);
                        } else {
                            // fragment is not used anymore
                            people[i] = null;
                        }
                    }
                    return people;
                }
            })
            .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<String, Person[]>() {
                @Override
                public void onPostExecute(String[] params, final Person[] person) {
                    addOrganizersToUI(person);
                    setIsLoading(false);
                }
            });

    @NonNull
    public static InfoFragment newInstance(@NonNull String plusId) {
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

        final String chapterPlusId = getArguments().getString(Const.EXTRA_PLUS_ID);
        if (chapterPlusId != null && Utils.isOnline(getActivity())) {
            new Builder<>(String.class, Person.class)
                    .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, Person>() {
                        @Nullable
                        @Override
                        public Person doInBackground(String... params) {
                            if (isAdded()) {
                                return GdgNavDrawerActivity.getPersonSync(chapterPlusId);
                            } else {
                                // fragment is not used anymore
                                return null;
                            }
                        }
                    })
                    .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<String, Person>() {
                        @Override
                        public void onPostExecute(String[] params, @Nullable Person person) {
                            if (person != null && getActivity() != null) {
                                updateChapterUIFrom(person);
                                updateOrganizersOnline(person);
                            }
                        }
                    })
                    .buildAndExecute();
        } else {
            App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_PERSON + chapterPlusId, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    final Person chachedChapter = (Person) item;
                    updateChapterUIFrom(chachedChapter);

                    for (int chapterIndex = 0; chapterIndex < chachedChapter.getUrls().size(); chapterIndex++) {
                        Person.Urls url = chachedChapter.getUrls().get(chapterIndex);
                        if (url.getValue().contains("plus.google.com/") && !url.getValue().contains("communities")) {
                            String org = url.getValue();
                            try {
                                String id = getGPlusIdFromPersonUrl(url);
                                final int indexAsFinal = chapterIndex;
                                App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_PERSON + id, false, new ModelCache.CacheListener() {
                                    @Override
                                    public void onGet(Object item) {
                                        addOrganizerToUI((Person) item);
                                        if (indexAsFinal == chachedChapter.getUrls().size()) {
                                            setIsLoading(false);
                                        }
                                    }

                                    @Override
                                    public void onNotFound(String key) {
                                        addUnknowOrganizerToUI();
                                        if (indexAsFinal == chachedChapter.getUrls().size()) {
                                            setIsLoading(false);
                                        }
                                    }
                                });
                            } catch (Exception ex) {
                                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.bogus_organizer, org),
                                        Snackbar.LENGTH_SHORT);
                                ColoredSnackBar.alert(snackbar).show();
                            }
                        }
                    }
                }

                @Override
                public void onNotFound(String key) {
                    if (getView() != null) {
                        Snackbar snackbar = Snackbar.make(getView(), R.string.offline_alert,
                                Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.alert(snackbar).show();
                    }
                }
            });
        }
    }

    private void addOrganizersToUI(@Nullable final Person[] people) {
        if (people == null) {
            addUnknowOrganizerToUI();
        } else {
            for (Person person : people) {
                addOrganizerToUI(person);
            }
        }
    }

    private void addUnknowOrganizerToUI() {
        Timber.d("null person");
        View v = getUnknownOrganizerView();
        mOrganizerBox.addView(v);
    }

    private void addOrganizerToUI(@Nullable final Person organizer) {
        if (organizer == null) {
            addUnknowOrganizerToUI();
        } else {
            View v = createOrganizerView(organizer);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + organizer.getId() + "/posts")));
                }
            });
            registerForContextMenu(v);
            if (mOrganizerBox != null) {
                mOrganizerBox.addView(v);
            }
        }
    }

    private void updateChapterUIFrom(@NonNull final Person person) {
        if (mTagline != null) {
            mTagline.setText(person.getTagline());
        }
        if (mAbout != null) {
            mAbout.setText(getAboutText(person));
        }
    }

    private Spanned getAboutText(@NonNull Person person) {
        String aboutText = person.getAboutMe();
        if (aboutText == null) {
            return SpannedString.valueOf("");
        }
        return Html.fromHtml(aboutText);
    }

    private void updateOrganizersOnline(@NonNull final Person person) {
        if (person.getUrls() != null) {
            for (Person.Urls url : person.getUrls()) {
                if (url.getValue().contains("plus.google.com/")) {
                    // TODO control the else statement
                    if (!url.getValue().contains("communities")) {
                        String org = url.getValue();
                        try {
                            String organizerParameter = getGPlusIdFromPersonUrl(url);
                            mFetchOrganizerInfo.addParameter(organizerParameter);
                        } catch (Exception ex) {
                            if (isAdded()) {
                                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.bogus_organizer, org),
                                        Snackbar.LENGTH_SHORT);
                                ColoredSnackBar.alert(snackbar).show();
                            }
                        }
                    }
                } else {
                    TextView tv = (TextView) mInflater.inflate(R.layout.list_resource_item, (ViewGroup) getView(), false);
                    tv.setText(Html.fromHtml("<a href='" + url.getValue() + "'>" + url.get("label") + "</a>"));
                    mResourcesBox.addView(tv);
                }

            }
            mFetchOrganizerInfo.buildAndExecute();
        }
    }

    private String getGPlusIdFromPersonUrl(@NonNull Person.Urls personUrl) {
        final String plusId = getArguments().getString(Const.EXTRA_PLUS_ID, "");
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
                        .replace(plusId, ""), "UTF-8").trim();
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
                    .replace(plusId, "").replaceAll("[^\\d.]", "").substring(0, 21);
        }
    }

    public void setIsLoading(boolean isLoading) {

        if (isLoading == mLoading || getActivity() == null) {
            return;
        }

        mLoading = isLoading;

        if (isLoading) {
            mContainer.setVisibility(View.GONE);
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
                    mProgressContainer.setVisibility(View.GONE);
                    mContainer.startAnimation(AnimationUtils.loadAnimation(
                            getActivity(), android.R.anim.fade_in));
                    mContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mProgressContainer.startAnimation(fadeOut);
        }
    }

    public View createOrganizerView(@Nullable Person person) {
        View convertView = mInflater.inflate(R.layout.list_organizer_item, (ViewGroup) getView(), false);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);

        if (person != null) {
            if (person.getImage() != null) {
                App.getInstance().getPicasso()
                        .load(person.getImage().getUrl())
                        .transform(new BitmapBorderTransformation(0,
                                getResources().getDimensionPixelSize(R.dimen.organizer_icon_size) / 2,
                                getResources().getColor(R.color.white)))
                        .placeholder(R.drawable.ic_no_avatar)
                        .into(picture);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(person.getDisplayName());
        }

        return convertView;
    }

    private View getUnknownOrganizerView() {
        View convertView = mInflater.inflate(R.layout.list_organizer_item, (ViewGroup) getView(), false);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);
        picture.setImageResource(R.drawable.ic_no_avatar);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(R.string.name_not_known);
        return convertView;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chapter_info, container, false);
        ButterKnife.bind(this, v);
        return v;
    }
}
