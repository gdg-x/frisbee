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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewSwitcher;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.android.gms.plus.GooglePlusUtil;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.joda.time.DateTime;
import roboguice.inject.InjectView;

import java.util.Collections;
import java.util.List;

public class FirstStartStep1Fragment extends RoboSherlockFragment {

    private static final String LOG_TAG = "GDG-FirstStartStep1Fragment";

    private static final int SWITCHER_CHILD_BUTTON = 1;

    private ApiRequest mFetchChaptersTask;
    private GroupDirectory mClient;
    private Chapter mSelectedChapter;

    @InjectView(R.id.chapter_list)
    private ListView mChapterListView;

    @InjectView(R.id.confirm)
    Button mConfirm;

    @InjectView(R.id.viewSwitcher)
    ViewSwitcher mLoadSwitcher;

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

        if(mSelectedChapter != null)
            outState.putParcelable("selected_chapter", mSelectedChapter);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onActivityCreated()");

        super.onActivityCreated(savedInstanceState);

        int errorCode = GooglePlusUtil.checkGooglePlusApp(getActivity());
        if (errorCode != GooglePlusUtil.SUCCESS) {
            GooglePlusUtil.getErrorDialog(errorCode, getActivity(), 0).show();
        }

        mClient = new GroupDirectory();

        if(savedInstanceState != null)
            mSelectedChapter = savedInstanceState.getParcelable("selected_chapter");

        mConfirm.setEnabled(mSelectedChapter != null);

        mFetchChaptersTask = mClient.getDirectory(new Response.Listener<Directory>() {
                  @Override
                  public void onResponse(final Directory directory) {
                      App.getInstance().getModelCache().putAsync("chapter_list", directory, DateTime.now().plusDays(4), new ModelCache.CachePutListener() {
                          @Override
                          public void onPutIntoCache() {
                              addChapters(directory.getGroups());

                              mLoadSwitcher.setDisplayedChild(SWITCHER_CHILD_BUTTON);
                          }
                      });

                  }
              }, new Response.ErrorListener() {
                  @Override
                  public void onErrorResponse(VolleyError volleyError) {
                      Crouton.makeText(getActivity(), getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                      Log.e(LOG_TAG, "Couldn't fetch chapter list", volleyError);
                  }
              });

        App.getInstance().getModelCache().getAsync("chapter_list", new ModelCache.CacheListener() {
            @Override
            public void onGet(Object item) {
                Directory directory = (Directory) item;
                mLoadSwitcher.setDisplayedChild(SWITCHER_CHILD_BUTTON);
                addChapters(directory.getGroups());
            }

            @Override
            public void onNotFound(String key) {
                mFetchChaptersTask.execute();
            }
        });

        mChapterListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mConfirm.setEnabled(true);
                mSelectedChapter = (Chapter) mChapterListView.getItemAtPosition(position);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getArguments().putParcelable("selected_chapter", mSelectedChapter);

                if (getActivity() instanceof Step1Listener)
                    ((Step1Listener) getActivity()).onConfirmedChapter(mSelectedChapter);
            }
        });
    }

    private int findSelectedChapterPosition() {
        if (mSelectedChapter == null)
            return -1;

        Log.d(LOG_TAG, "Searing selected chapter from " + mChapterListView.getCount() + " entries");
        for (int i = 0; i < mChapterListView.getCount(); ++i) {
            if (mSelectedChapter.compareTo((Chapter)mChapterListView.getItemAtPosition(i)) == 0)
                return i;
        }
        return -1;
    }

    private void scrollToSelectedChapter(int position) {
        Log.d(LOG_TAG, "Scrolling to position " + position);
        if (mChapterListView.getCount() == 0 || position == -1)
            return;
        int item_size = mChapterListView.getAdapter().getView(0, null, mChapterListView).getMeasuredHeight();
        Log.d(LOG_TAG, "Item size is " + item_size);
        // We want to show selected item somewhere in the middle, unless it's first one
        mChapterListView.scrollTo(0, Math.max(0, item_size*(position-3)));
    }

    private void addChapters(List<Chapter> chapterList) {
        // For searching GDG Home city it's better to sort list basing on names rather then location
        // TODO: Introduce CountryCity comparator to sort first by country and then by city
        // TODO: this will allow to create list groups
        Collections.sort(chapterList);
        Object objArray[] = chapterList.toArray();
        ArrayAdapter<Object> a = new ArrayAdapter<Object>(FirstStartStep1Fragment.this.getActivity(),
                android.R.layout.simple_list_item_1, objArray);
        mChapterListView.setAdapter(a);

        int position = findSelectedChapterPosition();
        if (position != -1) {
            mChapterListView.requestFocusFromTouch();
            mChapterListView.setSelection(position);
            scrollToSelectedChapter(position);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_step1, null);
    }

    public interface Step1Listener {
        void onConfirmedChapter(Chapter chapter);
    }
}
