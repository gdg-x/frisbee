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

package org.gdg.frisbee.android.arrow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.appstate.AppStateStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.BitmapBorderTransformation;
import org.gdg.frisbee.android.view.DividerItemDecoration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import timber.log.Timber;

public class ArrowTaggedActivity extends GdgActivity {

    public static final String ID_SEPARATOR_FOR_SPLIT = "\\|";
    public static final String ID_SPLIT_CHAR = "|";

    @Bind(R.id.taggedList)
    RecyclerView taggedList;
    @NonNull
    String taggedOrganizers = "";
    private OrganizerAdapter adapter;

    String[] orgas;

    @NonNull
    @Override
    protected String getTrackedViewName() {
        return "Arrow/Tagges";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow_tagged);
        getActionBarToolbar().setNavigationIcon(R.drawable.ic_up);
        adapter = new OrganizerAdapter();

        if (!PrefUtils.isSignedIn(this)) {
            finish();
        }

        taggedList.setLayoutManager(new LinearLayoutManager(this));
        taggedList.addItemDecoration(new DividerItemDecoration(this, null));
        taggedList.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AppStateManager.load(getGoogleApiClient(), Const.ARROW_DONE_STATE_KEY).setResultCallback(new ResultCallback<AppStateManager.StateResult>() {
            @Override
            public void onResult(@NonNull AppStateManager.StateResult stateResult) {
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
                    public void success(@NonNull final Directory directory, final retrofit.client.Response response) {
                        if (orgas != null) {
                            return;
                        }
                        orgas = taggedOrganizers.split("\\|");
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
                                            public void onResult(@NonNull People.LoadPeopleResult loadPeopleResult) {
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
                        Timber.e(error, "Error");
                    }
                });
            }
        });
    }

    private String mergeIds(@NonNull String list1, @NonNull String list2) {
        String[] parts1 = list1.split(ID_SEPARATOR_FOR_SPLIT);
        String[] parts2 = list2.split(ID_SEPARATOR_FOR_SPLIT);
        Set<String> mergedSet = new HashSet<>(Arrays.asList(parts1));
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

    public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.ViewHolder> {

        // SparseArray is recommended by most Android Developer Advocates
        // as it has reduced memory usage
        private SparseArrayCompat<Organizer> organizers;

        public OrganizerAdapter() {
            organizers = new SparseArrayCompat<>();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.list_tagged_organizer_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final Organizer o = organizers.get(position);

            App.getInstance().getPicasso()
                    .load(o.resolved.getImage().getUrl())
                    .transform(new BitmapBorderTransformation(0,
                            getResources().getDimensionPixelSize(R.dimen.list_item_avatar) / 2,
                            getResources().getColor(R.color.white)))
                    .into(holder.avatar);

            holder.name.setText(o.resolved.getDisplayName());
            holder.chapter.setText(o.chapterName);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/" + o.plusId + "/posts")));
                }
            });
        }

        @Override
        public int getItemCount() {
            return organizers.size();
        }

        public void add(Organizer o) {
            organizers.append(organizers.size(), o);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            @Bind(R.id.avatar)
            ImageView avatar;

            @Bind(R.id.organizerName)
            TextView name;

            @Bind(R.id.organizerChapter)
            TextView chapter;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
