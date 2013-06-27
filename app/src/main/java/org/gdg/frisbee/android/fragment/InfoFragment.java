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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.api.GapiTransportChooser;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.GdgVolley;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.task.Builder;
import org.gdg.frisbee.android.task.CommonAsyncTask;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;
import roboguice.inject.InjectView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 04:57
 */
public class InfoFragment extends RoboSherlockFragment {

    private static final String LOG_TAG = "GDG-InfoFragment";

    final HttpTransport mTransport = GapiTransportChooser.newCompatibleTransport();
    final JsonFactory mJsonFactory = new GsonFactory();

    private Plus mClient;

    private GoogleMap mMap;

    @InjectView(R.id.about)
    private TextView mAbout;

    @InjectView(R.id.organizer_box)
    private LinearLayout mOrganizerBox;

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

                                Log.d(LOG_TAG, "Get Organizer " + params[i]);
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
                .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<Person[]>() {
                    @Override
                    public void onPostExecute(final Person[] person) {
                        if(person == null) {
                            Log.d(LOG_TAG, "null person");
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
                                request.setFields("aboutMe,circledByCount,cover/coverPhoto/url,currentLocation,displayName,emails/primary,plusOneCount,tagline,urls");
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
                .setOnPostExecuteListener(new CommonAsyncTask.OnPostExecuteListener<Person>() {
                    @Override
                    public void onPostExecute(Person person) {
                        if(person != null) {
                            mAbout.setText(Html.fromHtml(person.getAboutMe()));

                            for(Person.Urls url: person.getUrls()) {
                                if(url.getValue().contains("plus.google.com/") && !url.getValue().contains("communities")) {
                                    String org = url.getValue();
                                    try {
                                    mFetchOrganizerInfo.addParameter(url.getValue().replace("plus.google.com/", "").replace("posts","").replace("/","").replace("about","").replace("u1","").replace("u0","").replace("https:","").replace("http:","").replace(getArguments().getString("plus_id"), "").replaceAll("[^\\d.]", "").substring(0,21));
                                    } catch(Exception ex) {
                                        Crouton.makeText(getActivity(), String.format(getString(R.string.bogus_organizer), org), Style.ALERT);
                                    }
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
                    Person person = (Person)item;

                    mAbout.setText(Html.fromHtml(person.getAboutMe()));
                    Crouton.makeText(getActivity(), getString(R.string.cached_content), Style.INFO).show();

                    for(Person.Urls url: person.getUrls()) {
                        if(url.getValue().contains("plus.google.com/") && !url.getValue().contains("communities")) {
                            String org = url.getValue();
                            try {
                                String id = url.getValue().replace("plus.google.com/", "").replace("posts","").replace("/","").replace("about","").replace("u1","").replace("u0","").replace("https:","").replace("http:","").replace(getArguments().getString("plus_id"), "").replaceAll("[^\\d.]", "").substring(0,21);

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
                                    }

                                    @Override
                                    public void onNotFound(String key) {
                                        //To change body of implemented methods use File | Settings | File Templates.
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

    public View getOrganizerView(Person item) {
        View convertView = mInflater.inflate(R.layout.list_organizer_item, null);

        ImageView picture = (ImageView) convertView.findViewById(R.id.icon);
        App.getInstance().getPicasso()
                .load(item.getImage().getUrl())
                .into(picture);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(item.getDisplayName());

        return convertView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chapter_info, null);
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
