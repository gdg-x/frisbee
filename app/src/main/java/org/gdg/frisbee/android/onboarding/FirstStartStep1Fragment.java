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
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Toast;
import android.widget.ViewSwitcher;

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
import org.gdg.frisbee.android.view.ColoredSnackBar;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;

public class FirstStartStep1Fragment extends BaseFragment {

    private static final String ARG_SELECTED_CHAPTER = "selected_chapter";
    @BindView(R.id.chapter_spinner)
    AutoCompleteSpinnerView mChapterSpinnerView;
    @BindView(R.id.chapter_spinner_text_input_layout)
    TextInputLayout mChapterSpinnerTextInputLayout;
    @BindView(R.id.confirm)
    Button mConfirmButton;
    @BindView(R.id.viewSwitcher)
    ViewSwitcher mLoadSwitcher;
    private ChapterAdapter mChapterAdapter;
    private Chapter mSelectedChapter;
    private ChapterComparator mLocationComparator;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mChapterAdapter != null && mChapterAdapter.getCount() > 0) {
            outState.putParcelable(ARG_SELECTED_CHAPTER, mSelectedChapter);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mLocationComparator = new ChapterComparator(PrefUtils.getHomeChapterId(getActivity()),
            App.getInstance().getLastLocation());

        mChapterAdapter = new ChapterAdapter(getActivity(), R.layout.spinner_item_welcome);
        mChapterAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        if (savedInstanceState != null) {
            mSelectedChapter = savedInstanceState.getParcelable(ARG_SELECTED_CHAPTER);
        }

        App.getInstance().getModelCache().getAsync(
            ModelCache.KEY_CHAPTER_LIST_HUB, new ModelCache.CacheListener() {
                @Override
                public void onGet(Object item) {
                    Directory directory = (Directory) item;
                    if (isContextValid()) {
                        addChapters(directory.getGroups());
                        mLoadSwitcher.setDisplayedChild(1);
                    }
                }

                @Override
                public void onNotFound(String key) {
                    fetchChapters();
                }
            }
        );

        mChapterSpinnerView.setThreshold(1);

        Filter.FilterListener enableConfirmOnUniqueFilterResult = new Filter.FilterListener() {
            @Override
            public void onFilterComplete(int count) {
                mConfirmButton.setEnabled(count == 1);
                if (count == 1) {
                    mSelectedChapter = mChapterAdapter.getItem(0);
                    updateAutoCompleteHint(mSelectedChapter);
                    mChapterSpinnerView.dismissDropDown();
                } else if (count == 0 && hasTrailingSpace(mChapterSpinnerView)) {
                    mChapterSpinnerTextInputLayout.setError(getString(R.string.remove_trailing_spaces));
                } else {
                    resetAutoCompleteHint();
                }
            }
        };
        AdapterView.OnItemClickListener enableConfirmOnChapterClick = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mConfirmButton.setEnabled(true);
                mSelectedChapter = mChapterAdapter.getItem(position);
                updateAutoCompleteHint(mSelectedChapter);
            }
        };

        mChapterSpinnerView.setFilterCompletionListener(enableConfirmOnUniqueFilterResult);
        mChapterSpinnerView.setOnItemClickListener(enableConfirmOnChapterClick);
        mChapterSpinnerView.setOnTouchListener(new ChapterSpinnerTouchListener());

        mChapterSpinnerTextInputLayout.setErrorEnabled(true);

        mConfirmButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getActivity() instanceof Step1Listener) {
                        //TODO re-order cached chapter list
                        ((Step1Listener) getActivity()).onConfirmedChapter(mSelectedChapter);
                    }
                }
            }
        );
    }

    private boolean hasTrailingSpace(AutoCompleteSpinnerView chapterSpinnerView) {
        return chapterSpinnerView.getText().toString().endsWith(" ");
    }

    private void updateAutoCompleteHint(Chapter selectedChapter) {
        mChapterSpinnerTextInputLayout.setHint(getString(R.string.home_gdg_with_city, selectedChapter.toString()));
        mChapterSpinnerTextInputLayout.setError(null);
    }

    private void resetAutoCompleteHint() {
        mChapterSpinnerTextInputLayout.setHint(getString(R.string.home_gdg));
        mChapterSpinnerTextInputLayout.setError(null);
    }

    private void fetchChapters() {

        App.getInstance().getGdgXHub().getDirectory().enqueue(new Callback<Directory>() {
            @Override
            public void success(Directory directory) {

                if (isContextValid()) {
                    addChapters(directory.getGroups());
                    mLoadSwitcher.setDisplayedChild(1);
                }
                App.getInstance().getModelCache().putAsync(ModelCache.KEY_CHAPTER_LIST_HUB,
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
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
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
        mChapterAdapter.clear();
        mChapterAdapter.addAll(chapterList);

        mChapterSpinnerView.setAdapter(mChapterAdapter);

        if (mSelectedChapter == null) {
            //if the location is available, select the first chapter by default.
            if (App.getInstance().getLastLocation() != null && chapterList.size() > 0) {
                mSelectedChapter = chapterList.get(0);
            }
        }
        if (mSelectedChapter != null) {
            mChapterSpinnerView.setText(mSelectedChapter.toString());
        } else {
            mChapterSpinnerView.showDropDown();
            mConfirmButton.setEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateView(inflater, R.layout.fragment_welcome_step1, container);
    }

    public interface Step1Listener {
        void onConfirmedChapter(Chapter chapter);
    }

    private class ChapterSpinnerTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int drawableRight = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (mChapterSpinnerView.getRight()
                    - mChapterSpinnerView.getCompoundDrawables()[drawableRight].getBounds().width())) {
                    mChapterSpinnerView.setText("");
                    resetAutoCompleteHint();
                    mChapterSpinnerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mChapterSpinnerView.showDropDown();
                        }
                    }, 100);
                    return true;
                }
            }
            return false;
        }
    }
}
