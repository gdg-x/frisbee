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

package org.gdg.frisbee.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.GapiTransportChooser;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 04:57
 */
public class InfoFragment extends Fragment {

    private static final String LOG_TAG = "GDG-InfoFragment";

    final HttpTransport mTransport = GapiTransportChooser.newCompatibleTransport();
    final JsonFactory mJsonFactory = new GsonFactory();

    private Plus mClient;

    private GoogleMap mMap;

    @InjectView(R.id.about)
    TextView mAbout;

    @InjectView(R.id.tagline)
    TextView mTagline;

    @InjectView(R.id.organizer_box)
    LinearLayout mOrganizerBox;

    @InjectView(R.id.resources_box)
    LinearLayout mResourcesBox;

    @InjectView(R.id.loading)
    LinearLayout mProgressContainer;

    @InjectView(R.id.container)
    ScrollView mContainer;

    private boolean mLoading = true;

    private LayoutInflater mInflater;

    private Builder<String, Person[]> mFetchOrganizerInfo;

    public static InfoFragment newInstance(String plusId) {
        InfoFragment fragment = new InfoFragment();
        Bundle arguments = new Bundle();
        arguments.putString("plus_id", plusId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mInflater = LayoutInflater.from(getActivity());

        mClient = new Plus.Builder(mTransport, mJsonFactory, null).setGoogleClientRequestInitializer(new CommonGoogleJsonClientRequestInitializer(getString(R.string.ip_simple_api_access_key))).setApplicationName("GDG Frisbee").build();

        if(Utils.isOnline(getActivity())) {
        mFetchOrganizerInfo = new Builder<String, Person[]>(String.class, Person[].class)
                .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, Person[]>() {
                    @Override
                    public Person[] doInBackground(String... params) {
                        if(params == null)
                            return null;
                        try {
                            Person[] people = new Person[params.length];
                            for(int i = 0; i < params.length; i++) {
                                Person person = (Person) App.getInstance().getModelCache().get("person_"+params[i]);

                                Timber.d("Get Organizer " + params[i]);
                                if(person == null) {
                                    Plus.People.Get request = mClient.people().get(params[i]);
                                    request.setFields("displayName,image,id");
                                    person = request.execute();

                                    App.getInstance().getModelCache().put("person_" + params[i], person, DateTime.now().plusDays(2));
                                }
                                people[i] = person;
                            }
                            return people;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<String, Person[]>() {
                    @Override
                    public void onPostExecute(String[] params, final Person[] person) {
                        if(person == null) {
                            Timber.d("null person");
                            View v = getUnknownOrganizerView();
                            mOrganizerBox.addView(v);
                            setIsLoading(false);
                            return;
                        }

                        for(int i = 0; i < person.length; i++) {
                            View v = getOrganizerView(person[i]);
                            final int myi = i;
                            v.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/"+person[myi].getId()+"/posts")));
                                }
                            });
                            registerForContextMenu(v);
                            mOrganizerBox.addView(v);
                        }
                        setIsLoading(false);
                    }
                });

        new Builder<String, Person>(String.class, Person.class)
                .addParameter(getArguments().getString("plus_id"))
                .setOnBackgroundExecuteListener(new CommonAsyncTask.OnBackgroundExecuteListener<String, Person>() {
                    @Override
                    public Person doInBackground(String... params) {
                        try {
                            Person person = (Person) App.getInstance().getModelCache().get("person_"+params[0]);

                            if(person == null) {
                                Plus.People.Get request = mClient.people().get(params[0]);
                                request.setFields("aboutMe,circledByCount,cover/coverPhoto/url,currentLocation,displayName,plusOneCount,tagline,urls");
                                person = request.execute();

                                App.getInstance().getModelCache().put("person_" + params[0], person, DateTime.now().plusDays(2));
                            }
                            return person;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<String, Person>() {
                    @Override
                    public void onPostExecute(String[] params, Person person) {
                        if(person != null && person.getUrls() != null) {
                            mTagline.setText(person.getTagline());
                            mAbout.setText(Html.fromHtml(person.getAboutMe()));

                            for(Person.Urls url: person.getUrls()) {
                                if(url.getValue().contains("plus.google.com/")){
                                    if(url.getValue().contains("communities")) {
                                        // TODO
                                    } else {
                                        String org = url.getValue();
                                        try {
                                            String organizerParameter = getUrlFromPersonUrl(url);
                                            mFetchOrganizerInfo.addParameter(organizerParameter);
                                        } catch(Exception ex) {
                                            if(isAdded())
                                                Crouton.makeText(getActivity(), String.format(getString(R.string.bogus_organizer), org), Style.ALERT);
                                        }
                                    }
                                } else {
                                    TextView tv = (TextView) mInflater.inflate(R.layout.list_resource_item, null);
                                    tv.setText(Html.fromHtml("<a href='" + url.getValue() + "'>" + url.get("label") + "</a>"));
                                    mResourcesBox.addView(tv);
                                }

                            }
                            mFetchOrganizerInfo.buildAndExecute();
                        }
                    }
                })
                .buildAndExecute();
        } else {
            App.getInstance().getModelCache().getAsync("person_"+getArguments().getString("plus_id"), false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    final Person chachedChapter = (Person)item;

                    mAbout.setText(Html.fromHtml(chachedChapter.getAboutMe()));
                    Crouton.makeText(getActivity(), getString(R.string.cached_content), Style.INFO).show();

                    for(int i = 0; i < chachedChapter.getUrls().size(); i++) {
                        Person.Urls url = chachedChapter.getUrls().get(i);
                        if(url.getValue().contains("plus.google.com/") && !url.getValue().contains("communities")) {
                            String org = url.getValue();
                            try {
                                String id = url.getValue().replace("plus.google.com/", "").replace("posts","").replace("/","").replace("about","").replace("u1","").replace("u0","").replace("https:","").replace("http:","").replace(getArguments().getString("plus_id"), "").replaceAll("[^\\d.]", "").substring(0,21);

                                final int finalI = i;
                                App.getInstance().getModelCache().getAsync("person_"+ id, false, new ModelCache.CacheListener() {
                                    @Override
                                    public void onGet(Object item) {
                                        final Person person = (Person)item;
                                        View v = getOrganizerView(person);
                                        v.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/"+person.getId()+"/posts")));
                                            }
                                        });
                                        registerForContextMenu(v);
                                        mOrganizerBox.addView(v);

                                        if( finalI == chachedChapter.getUrls().size()) {
                                            setIsLoading(false);
                                        }
                                    }

                                    @Override
                                    public void onNotFound(String key) {
                                        View v = getUnknownOrganizerView();
                                        mOrganizerBox.addView(v);
                                        if (finalI == chachedChapter.getUrls().size()) {
                                            setIsLoading(false);
                                        }
                                    }
                                });
                            } catch(Exception ex) {
                                Crouton.makeText(getActivity(), String.format(getString(R.string.bogus_organizer), org), Style.ALERT);
                            }
                        }
                    }
                }

                @Override
                public void onNotFound(String key) {
                    Crouton.makeText(getActivity(), getString(R.string.offline_alert), Style.ALERT).show();
                }
            });
        }
    }

    private String getUrlFromPersonUrl(Person.Urls personUrl) {
        if (personUrl.getValue().contains("+")) {
            try {
                return "+" + URLDecoder.decode(personUrl.getValue().replace("plus.google.com/", "").replace("posts", "").replace("/", "").replace("about", "").replace("u1", "").replace("u0", "").replace("https:", "").replace("http:", "").replace(getArguments().getString("plus_id"), ""), "UTF-8").trim();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return personUrl.getValue();
            }
        } else {
            return personUrl.getValue().replace("plus.google.com/", "").replace("posts", "").replace("/", "").replace("about", "").replace("u1", "").replace("u0", "").replace("https:", "").replace("http:", "").replace(getArguments().getString("plus_id"), "").replaceAll("[^\\d.]", "").substring(0, 21);
        }
    }

    public void setIsLoading(boolean isLoading) {

        if(isLoading == mLoading || getActivity() == null)
            return;

        mLoading = isLoading;

        if(isLoading) {
            mContainer.setVisibility(View.GONE);
            mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), android.R.anim.fade_in));
            mProgressContainer.setVisibility(View.VISIBLE);
        } else {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    mProgressContainer.setVisibility(View.GONE);
                    mContainer.startAnimation(AnimationUtils.loadAnimation(
                            getActivity(), android.R.anim.fade_in));
                    mContainer.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            mProgressContainer.startAnimation(fadeOut);
        }
    }

    public View getOrganizerView(Person item) {
        View convertView = mInflater.inflate(R.layout.list_organizer_item, null);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);

        if(item != null) {
            if(item.getImage() != null) {
                App.getInstance().getPicasso()
                        .load(item.getImage().getUrl())
                        .placeholder(R.drawable.ic_no_avatar)
                        .into(picture);
            }

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(item.getDisplayName());
        }

        return convertView;
    }

    private View getUnknownOrganizerView() {
        View convertView = mInflater.inflate(R.layout.list_organizer_item, null);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);
        picture.setImageResource(R.drawable.ic_no_avatar);
        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(R.string.name_not_known);
        return convertView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chapter_info, null);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }
}
