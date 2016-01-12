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
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.Callback;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.chapter.ChapterAdapter;
import org.gdg.frisbee.android.chapter.ChapterComparator;
import org.gdg.frisbee.android.common.BaseFragment;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.gdg.frisbee.android.view.AutoCompleteSpinnerView;
import org.gdg.frisbee.android.view.BaseTextWatcher;
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FirstStartStep1Fragment extends BaseFragment {

    private static final String ARG_SELECTED_CHAPTER = "selected_chapter";
    @Bind(R.id.chapter_spinner)
    AutoCompleteSpinnerView autoCompleteSpinnerView;
    @Bind(R.id.confirm)
    Button mConfirmButton;
    @Bind(R.id.viewSwitcher)
    ViewSwitcher mLoadSwitcher;
    private ChapterAdapter mChapterForCityName;
    private Chapter mSelectedChapter;
    private ChapterComparator mLocationComparator;

    private final TextWatcher disableConfirmAfterTextChanged = new BaseTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            mConfirmButton.setEnabled(false);
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mChapterForCityName != null && mChapterForCityName.getCount() > 0) {
            outState.putParcelable(ARG_SELECTED_CHAPTER, mSelectedChapter);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mLocationComparator = new ChapterComparator(PrefUtils.getHomeChapterId(getActivity()),
                App.getInstance().getLastLocation());

        mChapterForCityName = new ChapterAdapter(getActivity(), R.layout.spinner_item_welcome);
        mChapterForCityName.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        if (savedInstanceState != null) {
            mSelectedChapter = savedInstanceState.getParcelable(ARG_SELECTED_CHAPTER);
        }

        App.getInstance().getModelCache().getAsync(
                Const.CACHE_KEY_CHAPTER_LIST_HUB, new ModelCache.CacheListener() {
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
                }
        );

        autoCompleteSpinnerView.setThreshold(1);

        Filter.FilterListener enableConfirmOnUniqueFilterResult = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                mConfirmButton.setEnabled(count == 1);
                if (count == 1) {
                    mSelectedChapter = mChapterForCityName.getItem(0);
                }
            }
        };
        AdapterView.OnItemClickListener enableConfirmOnChapterClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedChapter = mChapterForCityName.getItem(position);
                mConfirmButton.setEnabled(true);
            }
        };

        autoCompleteSpinnerView.setFilterCompletionListener(enableConfirmOnUniqueFilterResult);
        autoCompleteSpinnerView.setOnItemClickListener(enableConfirmOnChapterClick);
        autoCompleteSpinnerView.addTextChangedListener(disableConfirmAfterTextChanged);

        autoCompleteSpinnerView.setOnTouchListener(new ChapterSpinnerTouchListener());

        mConfirmButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getActivity() instanceof Step1Listener) {
                            ((Step1Listener) getActivity()).onConfirmedChapter(mSelectedChapter);
                        }
                    }
                }
        );
    }

    private void fetchChapters() {

        App.getInstance().getGdgXHub().getDirectory().enqueue(new Callback<Directory>() {
            @Override
            public void success(Directory directory) {

                if (isContextValid()) {
                    addChapters(directory.getGroups());
                    mLoadSwitcher.setDisplayedChild(1);
                }
                App.getInstance().getModelCache().putAsync(Const.CACHE_KEY_CHAPTER_LIST_HUB,
                        directory,
                        DateTime.now().plusDays(4),
                        null);
            }

            @Override
            public void failure(Throwable error) {
                showError(R.string.fetch_chapters_failed);
            }

            @Override
            public void networkFailure(Throwable error) {
                showError(R.string.offline_alert);
            }
        });
    }

    @Override
    protected void showError(@StringRes int errorStringRes) {
        if (isContextValid()) {
            if (getView() != null) {
                Snackbar snackbar = Snackbar.make(getView(), errorStringRes,
                        Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fetchChapters();
                    }
                });
                ColoredSnackBar.alert(snackbar).show();
            } else {
                Toast.makeText(getActivity(), errorStringRes, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addChapters(List<Chapter> chapterList) {
        Collections.sort(chapterList, mLocationComparator);
        mChapterForCityName.clear();
        mChapterForCityName.addAll(chapterList);

        autoCompleteSpinnerView.setAdapter(mChapterForCityName);

        if (mSelectedChapter != null) {
            int pos = mChapterForCityName.getPosition(mSelectedChapter);
            autoCompleteSpinnerView.setSelection(pos);
        } else {
            //if the location is available, select the first chapter by default.
            if (App.getInstance().getLastLocation() != null && chapterList.size() > 0) {
                mSelectedChapter = chapterList.get(0);
            }
        }
        if (mSelectedChapter != null) {
            autoCompleteSpinnerView.setText(mSelectedChapter.toString(), true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome_step1, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    public interface Step1Listener {
        void onConfirmedChapter(Chapter chapter);
    }

    private class ChapterSpinnerTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int drawableRight = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (autoCompleteSpinnerView.getRight()
                        - autoCompleteSpinnerView.getCompoundDrawables()[drawableRight].getBounds().width())) {
                    autoCompleteSpinnerView.showDropDown();
                    return true;
                }
            }
            return false;
        }
    }
}
