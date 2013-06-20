package org.gdg.frisbee.android.fragment;

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
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.GingerbreadLastLocationFinder;
import org.joda.time.DateTime;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.fragment
 * <p/>
 * User: maui
 * Date: 14.06.13
 * Time: 02:52
 */
public class FirstStartStep1Fragment extends RoboSherlockFragment {

    private static String LOG_TAG = "GDG-FirstStartStep1Fragment";

    private GroupDirectory.ApiRequest mFetchChaptersTask;
    private ChapterAdapter mSpinnerAdapter;
    private GroupDirectory mClient;
    private GingerbreadLastLocationFinder mLocationFinder;
    private Location mLastLocation;

    private Step1Listener mListener;

    @InjectView(R.id.chapter_spinner)
    Spinner mChapterSpinner;

    @InjectView(R.id.confirm)
    Button mConfirm;

    @InjectView(R.id.viewSwitcher)
    ViewSwitcher mLoadSwitcher;

    private Comparator<Chapter> mLocationComparator = new Comparator<Chapter>() {
        @Override
        public int compare(Chapter chapter, Chapter chapter2) {
            float[] results = new float[1];
            float[] results2 = new float[1];

            if(mLastLocation == null)
                return chapter.getName().compareTo(chapter2.getName());

            if(chapter.getGeo() == null)
                return 1;
            if(chapter2.getGeo() == null)
                return -1;

            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), chapter.getGeo().getLat(), chapter.getGeo().getLng(), results);
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), chapter2.getGeo().getLat(), chapter2.getGeo().getLng(), results2);

            if(results[0] == results2[0])
                return 0;
            else if(results[0] > results2[0])
                return 1;
            else
                return -1;
        }
    };

    public static FirstStartStep1Fragment newInstance(Step1Listener listener) {
        FirstStartStep1Fragment fragment = new FirstStartStep1Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int errorCode = GooglePlusUtil.checkGooglePlusApp(getActivity());
        if (errorCode != GooglePlusUtil.SUCCESS) {
            GooglePlusUtil.getErrorDialog(errorCode, getActivity(), 0).show();
        }

        mClient = new GroupDirectory();
        mSpinnerAdapter = new ChapterAdapter(getActivity(), android.R.layout.simple_list_item_1);

        mLocationFinder = new GingerbreadLastLocationFinder(getActivity());
        mLastLocation = mLocationFinder.getLastBestLocation(5000,60*60*1000);
        mFetchChaptersTask = mClient.getDirectory(new Response.Listener<Directory>() {
                  @Override
                  public void onResponse(Directory directory) {
                      App.getInstance().getModelCache().putAsync("chapter_list", directory, DateTime.now().plusDays(4));

                      addChapters(directory.getGroups());
                      mChapterSpinner.setAdapter(mSpinnerAdapter);
                      mLoadSwitcher.setDisplayedChild(1);
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
                if (directory != null) {
                    mLoadSwitcher.setDisplayedChild(1);
                    addChapters(directory.getGroups());
                    mChapterSpinner.setAdapter(mSpinnerAdapter);
                } else {
                    mFetchChaptersTask.execute();
                }
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Chapter selectedChapter = (Chapter)mChapterSpinner.getSelectedItem();
                getArguments().putParcelable("selected_chapter", selectedChapter);

                if(mListener != null)
                    mListener.onConfirmedChapter(selectedChapter);
            }
        });
    }

    private void addChapters(List<Chapter> chapterList) {
        Collections.sort(chapterList, mLocationComparator);
        mSpinnerAdapter.addAll(chapterList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_step1, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    public void setListener(Step1Listener mListener) {
        this.mListener = mListener;
    }

    public interface Step1Listener {
        void onConfirmedChapter(Chapter chapter);
    }
}
