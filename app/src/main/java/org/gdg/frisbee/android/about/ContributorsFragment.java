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

package org.gdg.frisbee.android.about;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.GitHub;
import org.gdg.frisbee.android.api.model.ContributorList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.PeopleListFragment;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

public class ContributorsFragment extends PeopleListFragment {

    private static final String GITHUB_ORGANIZATION = "gdg-x";
    private static final String GITHUB_REPO = "frisbee";

    private GitHub github;
    private ModelCache modelCache;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        github = App.from(context).getGithub();
        modelCache = App.from(context).getModelCache();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadContributors();
    }

    protected void loadContributors() {
        if (Utils.isOnline(getActivity())) {
            fetchGitHubContributors();
        } else {
            modelCache.getAsync(
                ModelCache.KEY_FRISBEE_CONTRIBUTORS, false, new ModelCache.CacheListener() {
                    @Override
                    public void onGet(Object item) {
                        ContributorList contributors = (ContributorList) item;

                        Snackbar snackbar = Snackbar.make(getView(), R.string.cached_content, Snackbar.LENGTH_SHORT);
                        ColoredSnackBar.info(snackbar).show();
                        mAdapter.addAll(contributors);
                    }

                    @Override
                    public void onNotFound(String key) {
                        showError(R.string.offline_alert);
                    }
                });
        }
    }

    private void fetchGitHubContributors() {
        github.getContributors(GITHUB_ORGANIZATION, GITHUB_REPO)
            .enqueue(new Callback<ContributorList>() {
                @Override
                public void onSuccess(final ContributorList contributors) {

                    mAdapter.addAll(contributors);
                    modelCache.putAsync(ModelCache.KEY_FRISBEE_CONTRIBUTORS, contributors,
                        DateTime.now().plusDays(1));
                }
            });
    }
}
