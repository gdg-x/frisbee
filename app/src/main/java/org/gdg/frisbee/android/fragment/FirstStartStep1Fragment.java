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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.Collections;
import java.util.List;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.ChapterComparator;
import org.joda.time.DateTime;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;

public class FirstStartStep1Fragment extends Fragment {

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
        Timber.d("newInstance");
        FirstStartStep1Fragment fragment = new FirstStartStep1Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.d("onSaveInstanceState");
        super.onSaveInstanceState(outState);

        if(mSpinnerAdapter != null && mSpinnerAdapter.getCount() > 0)
            outState.putParcelable("selected_chapter", mSpinnerAdapter.getItem(mChapterSpinner.getSelectedItemPosition()));

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Timber.d("onActivityCreated()");

        super.onActivityCreated(savedInstanceState);


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
                      App.getInstance().getModelCache().putAsync("chapter_list_hub", directory, DateTime.now().plusDays(4), new ModelCache.CachePutListener() {
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
                      if (isDetached()){
                          Toast.makeText(getActivity(), R.string.fetch_chapters_failed, Toast.LENGTH_SHORT).show();
                      } else {
                          Crouton.makeText(getActivity(), getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                      }
                      Timber.e("Could'nt fetch chapter list", volleyError);
                  }
              });

        App.getInstance().getModelCache().getAsync("chapter_list_hub", new ModelCache.CacheListener() {
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
        View v = inflater.inflate(R.layout.fragment_welcome_step1, null);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public interface Step1Listener {
        void onConfirmedChapter(Chapter chapter);
    }
}
