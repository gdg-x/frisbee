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

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ViewSwitcher;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.android.gms.plus.GooglePlusUtil;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.ChapterComparator;
import org.gdg.frisbee.android.utils.GingerbreadLastLocationFinder;
import org.joda.time.DateTime;
import roboguice.inject.InjectView;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FirstStartStep1Fragment extends RoboSherlockFragment {

    private static String LOG_TAG = "GDG-FirstStartStep1Fragment";

    private ApiRequest mFetchChaptersTask;
    private ChapterAdapter mSpinnerAdapter;
    private GroupDirectory mClient;

    private Chapter mSelectedChapter;

    @InjectView(R.id.chapter_spinner)
    Spinner mChapterSpinner;

    @InjectView(R.id.confirm)
    Button mConfirm;

    @InjectView(R.id.viewSwitcher)
    ViewSwitcher mLoadSwitcher;

    private SharedPreferences mPreferences;
    private ChapterComparator mLocationComparator;

    public static FirstStartStep1Fragment newInstance() {
        Log.d(LOG_TAG, "newInstance");
        FirstStartStep1Fragment fragment = new FirstStartStep1Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

        if(mSpinnerAdapter != null && mSpinnerAdapter.getCount() > 0)
            outState.putParcelable("selected_chapter", mSpinnerAdapter.getItem(mChapterSpinner.getSelectedItemPosition()));

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onActivityCreated()");

        super.onActivityCreated(savedInstanceState);

        int errorCode = GooglePlusUtil.checkGooglePlusApp(getActivity());
        if (errorCode != GooglePlusUtil.SUCCESS) {
            GooglePlusUtil.getErrorDialog(errorCode, getActivity(), 0).show();
        }

        mPreferences = getActivity().getSharedPreferences("gdg", Context.MODE_PRIVATE);
        mLocationComparator = new ChapterComparator(mPreferences);

        mClient = new GroupDirectory();
        mSpinnerAdapter = new ChapterAdapter(getActivity(), android.R.layout.simple_list_item_1);

        if(savedInstanceState != null) {
            mSelectedChapter = savedInstanceState.getParcelable("selected_chapter");
        }

        mFetchChaptersTask = mClient.getDirectory(new Response.Listener<Directory>() {
                  @Override
                  public void onResponse(final Directory directory) {
                      App.getInstance().getModelCache().putAsync("chapter_list", directory, DateTime.now().plusDays(4), new ModelCache.CachePutListener() {
                          @Override
                          public void onPutIntoCache() {
                              addChapters(directory.getGroups());
                              mLoadSwitcher.setDisplayedChild(1);
                          }
                      });

                  }
              }, new Response.ErrorListener() {
                  @Override
                  public void onErrorResponse(VolleyError volleyError) {
                      Crouton.makeText(getActivity(), getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                      Log.e(LOG_TAG, "Could'nt fetch chapter list", volleyError);
                  }
              });

        App.getInstance().getModelCache().getAsync("chapter_list", new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Directory directory = (Directory) item;
                mLoadSwitcher.setDisplayedChild(1);
                addChapters(directory.getGroups());
            }

            @Override
            public void onNotFound(String key) {
                mFetchChaptersTask.execute();
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chapter selectedChapter = (Chapter) mChapterSpinner.getSelectedItem();
                getArguments().putParcelable("selected_chapter", selectedChapter);

                if (getActivity() instanceof Step1Listener)
                    ((Step1Listener) getActivity()).onConfirmedChapter(selectedChapter);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void addChapters(List<Chapter> chapterList) {
        Collections.sort(chapterList, mLocationComparator);
        mSpinnerAdapter.clear();
        mSpinnerAdapter.addAll(chapterList);

        mChapterSpinner.setAdapter(mSpinnerAdapter);

        if(mSelectedChapter != null) {
            int pos = mSpinnerAdapter.getPosition(mSelectedChapter);
            mChapterSpinner.setSelection(pos);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_step1, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public interface Step1Listener {
        void onConfirmedChapter(Chapter chapter);
    }
}
