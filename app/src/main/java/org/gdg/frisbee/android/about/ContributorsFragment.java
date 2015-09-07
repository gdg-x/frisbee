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

import android.os.Bundle;
import android.support.design.widget.Snackbar;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.ContributorList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.common.PeopleListFragment;
import org.gdg.frisbee.android.utils.Utils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

public class ContributorsFragment extends PeopleListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadContributors();
    }

    protected void loadContributors() {
        if (Utils.isOnline(getActivity())) {
            fetchGitHubContributors();
        } else {
            App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_FRISBEE_CONTRIBUTORS, false, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    ContributorList contributors = (ContributorList) item;

                    Snackbar snackbar = Snackbar.make(getView(), R.string.cached_content, Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.info(snackbar).show();
                    mAdapter.addAll(contributors);
                }

                @Override
                public void onNotFound(String key) {
                    Snackbar snackbar = Snackbar.make(getView(), R.string.offline_alert, Snackbar.LENGTH_SHORT);
                    ColoredSnackBar.alert(snackbar).show();
                }
            });
        }
    }
    
    private void fetchGitHubContributors() {
        App.getInstance().getGithub().getContributors(Const.GITHUB_ORGA, Const.GITHUB_REPO).enqueue(new Callback<ContributorList>() {
            @Override
            public void onSuccessResponse(ContributorList contributors) {

                mAdapter.addAll(contributors);
                App.getInstance().getModelCache().putAsync(Const.CACHE_KEY_FRISBEE_CONTRIBUTORS,
                        contributors,
                        DateTime.now().plusDays(1),
                        null);
            }
        });
    }
}
