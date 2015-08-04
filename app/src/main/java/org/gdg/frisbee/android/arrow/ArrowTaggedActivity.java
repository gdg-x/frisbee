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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.gms.appstate.AppStateManager;
import com.google.android.gms.appstate.AppStateStatusCodes;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.DividerItemDecoration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import retrofit.Callback;
import retrofit.RetrofitError;
import timber.log.Timber;

public class ArrowTaggedActivity extends GdgActivity {

    public static final String ID_SEPARATOR_FOR_SPLIT = "\\|";
    public static final String ID_SPLIT_CHAR = "|";

    @Bind(R.id.taggedList)
    RecyclerView taggedList;

    private String serializedOrganizers;
    private OrganizerAdapter adapter;

    @Override
    protected String getTrackedViewName() {
        return "Arrow/Tagges";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow_tagged);
        getActionBarToolbar().setNavigationIcon(R.drawable.ic_up);
        adapter = new OrganizerAdapter(this);

        if (!PrefUtils.isSignedIn(this)) {
            finish();
        }

        taggedList.setLayoutManager(new LinearLayoutManager(this));
        taggedList.addItemDecoration(new DividerItemDecoration(this, null));
        taggedList.setAdapter(adapter);

        AppStateManager.load(getGoogleApiClient(), Const.ARROW_DONE_STATE_KEY)
                .setResultCallback(new ResultCallback<AppStateManager.StateResult>() {

                    @Override
                    public void onResult(AppStateManager.StateResult stateResult) {
                        AppStateManager.StateConflictResult conflictResult = stateResult.getConflictResult();
                        AppStateManager.StateLoadedResult loadedResult = stateResult.getLoadedResult();
                        serializedOrganizers = "";

                        if (loadedResult != null) {
                            final int statusCode = loadedResult.getStatus().getStatusCode();
                            if (statusCode == AppStateStatusCodes.STATUS_OK) {
                                serializedOrganizers = new String(loadedResult.getLocalData());
                            }
                        } else if (conflictResult != null) {
                            serializedOrganizers = mergeIds(new String(conflictResult.getLocalData()),
                                    new String(conflictResult.getServerData()));
                        }

                        App.getInstance().getGdgXHub().getDirectory(new Callback<Directory>() {

                            @Override
                            public void success(final Directory directory, final retrofit.client.Response response) {
                                loadChapterOrganizers(directory);
                            }

                            @Override
                            public void failure(final RetrofitError error) {
                                Timber.e(error, "Error");
                            }
                        });
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadChapterOrganizers(Directory directory) {
        if (TextUtils.isEmpty(serializedOrganizers)) {
            return;
        }

        String[] organizers = serializedOrganizers.split(ID_SEPARATOR_FOR_SPLIT);

        for (String organizerId : organizers) {
            Chapter organizerChapter = null;
            for (Chapter chapter : directory.getGroups()) {
                if (chapter.getOrganizers().contains(organizerId)) {
                    organizerChapter = chapter;
                    break;
                }
            }

            if (organizerChapter != null) {
                final Organizer organizer = new Organizer();
                organizer.setPlusId(organizerId);
                organizer.setChapterName(organizerChapter.getName());
                organizer.setChapterId(organizerChapter.getGplusId());

                Plus.PeopleApi.load(getGoogleApiClient(), organizer.getPlusId())
                        .setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
                            @Override
                            public void onResult(People.LoadPeopleResult loadPeopleResult) {
                                organizer.setResolved(loadPeopleResult.getPersonBuffer().get(0));
                                adapter.add(organizer);
                                adapter.notifyDataSetChanged();
                            }
                        });
            }
        }
    }

    private String mergeIds(String list1, String list2) {
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
}
