/*
 * Copyright 2014-2015 The GDG Frisbee Project
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

package org.gdg.frisbee.android.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.appstate.AppStateStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.squareup.picasso.Picasso;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.PrefUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import timber.log.Timber;

public class ArrowTaggedActivity extends GdgNavDrawerActivity {

    public static final String ID_SEPARATOR_FOR_SPLIT = "\\|";
    public static final String ID_SPLIT_CHAR = "|";

    @InjectView(R.id.taggedList)
    ListView taggedList;
    String taggedOrganizers = "";
    private OrganizerAdapter adapter;

    @Override
    protected String getTrackedViewName() {
        return "Arrow/Tagges";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow_tagged);

        adapter = new OrganizerAdapter(this, 0);

        if (!PrefUtils.isSignedIn(this)) {
            finish();
        }

        taggedList.setAdapter(adapter);
        taggedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Organizer item = adapter.getItem(i);

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + item.plusId + "/posts")));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        AppStateManager.load(getGoogleApiClient(), Const.ARROW_DONE_STATE_KEY).setResultCallback(new ResultCallback<AppStateManager.StateResult>() {
            @Override
            public void onResult(AppStateManager.StateResult stateResult) {
                AppStateManager.StateConflictResult conflictResult = stateResult.getConflictResult();
                AppStateManager.StateLoadedResult loadedResult = stateResult.getLoadedResult();

                taggedOrganizers = "";
                if (loadedResult != null) {
                    final int statusCode = loadedResult.getStatus().getStatusCode();
                    if (statusCode == AppStateStatusCodes.STATUS_OK || statusCode == AppStateStatusCodes.STATUS_STATE_KEY_NOT_FOUND) {
                        taggedOrganizers = "";

                        if (statusCode == AppStateStatusCodes.STATUS_OK) {
                            taggedOrganizers = new String(loadedResult.getLocalData());
                        }
                    }
                } else if (conflictResult != null) {
                    taggedOrganizers = mergeIds(new String(conflictResult.getLocalData()), new String(conflictResult.getServerData()));
                }

                App.getInstance().getGdgXHub().getDirectory(new Callback<Directory>() {

                    @Override
                    public void success(final Directory directory, final retrofit.client.Response response) {
                        String[] orgas = taggedOrganizers.split("\\|");
                        for (String orga : orgas) {
                            Chapter orgaChapter = null;
                            for (Chapter c : directory.getGroups()) {
                                if (c.getOrganizers().contains(orga)) {
                                    orgaChapter = c;
                                    break;
                                }
                            }


                            if (orgaChapter != null) {
                                final Organizer organizer = new Organizer();
                                organizer.plusId = orga;
                                organizer.chapterName = orgaChapter.getName();
                                organizer.chapterId = orgaChapter.getGplusId();

                                Plus.PeopleApi.load(getGoogleApiClient(), organizer.plusId)
                                        .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                                            @Override
                                            public void onResult(People.LoadPeopleResult loadPeopleResult) {
                                                organizer.resolved = loadPeopleResult.getPersonBuffer().get(0);
                                                adapter.add(organizer);
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                            }

                        }
                    }

                    @Override
                    public void failure(final RetrofitError error) {
                        Timber.e("Error", error);
                    }
                });
            }
        });
    }

    private String mergeIds(String list1, String list2) {
        String[] parts1 = list1.split(ID_SEPARATOR_FOR_SPLIT);
        String[] parts2 = list2.split(ID_SEPARATOR_FOR_SPLIT);
        Set<String> mergedSet = new HashSet<String>(Arrays.asList(parts1));
        mergedSet.addAll(Arrays.asList(parts2));
        return TextUtils.join(ID_SPLIT_CHAR, mergedSet);
    }

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
    }

    public class Organizer {
        public String plusId;
        public String chapterId;
        public String chapterName;
        public Person resolved;
    }

    public class OrganizerAdapter extends ArrayAdapter<Organizer> {

        private LayoutInflater inflater;

        public OrganizerAdapter(Context context, int resource) {
            super(context, resource);

            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_tagged_organizer_item, parent, false);
            }

            Organizer o = getItem(position);

            ImageView avatar = (ImageView) convertView.findViewById(R.id.avatar);
            TextView name = (TextView) convertView.findViewById(R.id.organizerName);
            TextView chapter = (TextView) convertView.findViewById(R.id.organizerChapter);

            Picasso.with(getContext()).load(o.resolved.getImage().getUrl())
                    .into(avatar);

            name.setText(o.resolved.getDisplayName());
            chapter.setText(o.chapterName);


            return convertView;
        }


    }
}
