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
import android.widget.Toast;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.Snapshots;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.plus.Person;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.common.GdgActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.DividerItemDecoration;

import java.io.IOException;

import butterknife.Bind;
import timber.log.Timber;

public class ArrowTaggedActivity extends GdgActivity {

    private static final String ID_SEPARATOR_FOR_SPLIT = "\\|";

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
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);

        if (TextUtils.isEmpty(serializedOrganizers)) {
            loadFromSnapshot();
        }
    }

    private void loadFromSnapshot() {
        Games.Snapshots.open(getGoogleApiClient(), Const.GAMES_SNAPSHOT_ID, false).setResultCallback(
            new ResultCallback<Snapshots.OpenSnapshotResult>() {

                @Override
                public void onResult(Snapshots.OpenSnapshotResult stateResult) {
                    if (!stateResult.getStatus().isSuccess()) {
                        return;
                    }

                    Snapshot loadedResult = stateResult.getSnapshot();
                    serializedOrganizers = "";
                    if (loadedResult != null) {
                        try {
                            serializedOrganizers = new String(loadedResult.getSnapshotContents().readFully());
                        } catch (IOException e) {
                            Timber.w(e, "Could not store tagged organizer");
                            Toast.makeText(ArrowTaggedActivity.this, R.string.arrow_oops, Toast.LENGTH_LONG).show();
                        }
                    }

                    App.getInstance().getGdgXHub().getDirectory().enqueue(new Callback<Directory>() {
                        @Override
                        public void success(Directory directory) {
                            loadChapterOrganizers(directory);
                        }
                    });
                }
            });
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

                App.getInstance().getPlusApi().getPerson(organizer.getPlusId()).enqueue(new Callback<Person>() {
                    @Override
                    public void success(Person person) {
                        organizer.setResolved(person);
                        adapter.add(organizer);
                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    }
                });
            }
        }
    }
}
