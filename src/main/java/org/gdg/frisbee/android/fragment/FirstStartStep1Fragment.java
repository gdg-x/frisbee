package org.gdg.frisbee.android.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ChapterAdapter;
import org.gdg.frisbee.android.api.GroupDirectory;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.app.App;
import org.joda.time.DateTime;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.Collections;

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

    @InjectView(R.id.chapter_spinner)
    Spinner mChapterSpinner;

    public static FirstStartStep1Fragment newInstance() {
        FirstStartStep1Fragment fragment = new FirstStartStep1Fragment();
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mClient = new GroupDirectory();
        mSpinnerAdapter = new ChapterAdapter(getActivity(), android.R.layout.simple_list_item_1);

        mFetchChaptersTask = mClient.getDirectory(new Response.Listener<Directory>() {
                                                      @Override
                                                      public void onResponse(Directory directory) {
                                                          App.getInstance().getModelCache().putAsync("chapter_list", directory, DateTime.now().plusDays(4));

                                                          ArrayList<Chapter> chapters = directory.getGroups();
                                                          Collections.sort(chapters);
                                                          mSpinnerAdapter.addAll(chapters);

                                                          mChapterSpinner.setAdapter(mSpinnerAdapter);

                                                      }
                                                  }, new Response.ErrorListener() {
                                                      @Override
                                                      public void onErrorResponse(VolleyError volleyError) {
                                                          Crouton.makeText(getActivity(), getString(R.string.fetch_chapters_failed), Style.ALERT).show();
                                                          Log.e(LOG_TAG, "Could'nt fetch chapter list", volleyError);
                                                      }
                                                  });
        mFetchChaptersTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome_step1, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
