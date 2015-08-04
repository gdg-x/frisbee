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

package org.gdg.frisbee.android.onboarding;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.chapter.ChapterAdapter;
import org.gdg.frisbee.android.chapter.ChapterComparator;
import org.gdg.frisbee.android.common.BaseFragment;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

public class FirstStartStep1Fragment extends BaseFragment {

    private static final String ARG_SELECTED_CHAPTER = "selected_chapter";
    @Bind(R.id.chapter_spinner)
    Spinner mChapterSpinner;
    @Bind(R.id.confirm)
    Button mConfirm;
    @Bind(R.id.viewSwitcher)
    ViewSwitcher mLoadSwitcher;
    private ChapterAdapter mSpinnerAdapter;
    @Nullable
    private Chapter mSelectedChapter;
    private ChapterComparator mLocationComparator;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSpinnerAdapter != null && mSpinnerAdapter.getCount() > 0) {
            outState.putParcelable(ARG_SELECTED_CHAPTER, mSpinnerAdapter.getItem(mChapterSpinner.getSelectedItemPosition()));
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mLocationComparator = new ChapterComparator(PrefUtils.getHomeChapterIdNotNull(getActivity()));

        mSpinnerAdapter = new ChapterAdapter(getActivity(), R.layout.spinner_item_welcome);
        mSpinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        if (savedInstanceState != null) {
            mSelectedChapter = savedInstanceState.getParcelable(ARG_SELECTED_CHAPTER);
        }

        App.getInstance().getModelCache().getAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB, new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Directory directory = (Directory) item;
                addChapters(directory.getGroups());
                mLoadSwitcher.setDisplayedChild(1);
            }

            @Override
            public void onNotFound(String key) {
                fetchChapters();
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chapter selectedChapter = (Chapter) mChapterSpinner.getSelectedItem();

                if (getActivity() instanceof Step1Listener) {
                    ((Step1Listener) getActivity()).onConfirmedChapter(selectedChapter);
                }
            }
        });
    }
    
    private void fetchChapters() {

        App.getInstance().getGdgXHub().getDirectory(new Callback<Directory>() {

            @Override
            public void success(@NonNull final Directory directory, Response response) {

                App.getInstance().getModelCache().putAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB,
                        directory,
                        DateTime.now().plusDays(4),
                        new ModelCache.CachePutListener() {
                            @Override
                            public void onPutIntoCache() {
                                addChapters(directory.getGroups());
                                mLoadSwitcher.setDisplayedChild(1);
                            }
                        });
            }

            @Override
            public void failure(RetrofitError error) {
                if (getView() != null) {
                    Snackbar snackbar = Snackbar.make(getView(), R.string.fetch_chapters_failed,
                            Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fetchChapters();
                        }
                    });
                    ColoredSnackBar.alert(snackbar).show();
                } else {
                    Toast.makeText(getActivity(), R.string.fetch_chapters_failed, Toast.LENGTH_SHORT).show();
                }
                Timber.e(error, "Could'nt fetch chapter list");
            }
        });
    }

    private void addChapters(@NonNull List<Chapter> chapterList) {
        Collections.sort(chapterList, mLocationComparator);
        mSpinnerAdapter.clear();
        mSpinnerAdapter.addAll(chapterList);

        mChapterSpinner.setAdapter(mSpinnerAdapter);

        if (mSelectedChapter != null) {
            int pos = mSpinnerAdapter.getPosition(mSelectedChapter);
            mChapterSpinner.setSelection(pos);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome_step1, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    public interface Step1Listener {
        void onConfirmedChapter(Chapter chapter);
    }
}
