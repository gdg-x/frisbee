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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.adapter.ContributorAdapter;
import org.gdg.frisbee.android.api.ApiRequest;
import org.gdg.frisbee.android.api.GitHub;
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.crowdin.model.Translator;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import timber.log.Timber;


public class TranslatorsFragment extends GdgListFragment {

    private static final String LOG_TAG = "GDG-TranslatorsFragment";
    private static final Contributor[] TRANSLATORS = new Contributor[]{
            new Translator("friedger", "https://www.gravatar.com/avatar/72c15c247727ba65f72de3c7c58c4a42?s=170&d=https%3A%2F%2Fcrowdin.com%2Fimages%2Fuser-picture.png", 103),
            new Translator("tasomaniac","https://www.gravatar.com/avatar/67be1b058c66ed002ff45a1f9a22c0ff?s=150&d=https%3A%2F%2Fcrowdin.com%2Fimages%2Fuser-picture.png", 35),
            new Translator("mauimauer", "https://www.gravatar.com/avatar/b9236795d95774ca2137bb15d54da0a9?s=150&d=https%3A%2F%2Fcrowdin.com%2Fimages%2Fuser-picture.png", 13),
            new Translator("Andrulko", "", 2)
    };

    private LayoutInflater mInflater;
    private ContributorAdapter mAdapter;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.d("onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Timber.d("onActivityCreated()");

        mAdapter = new ContributorAdapter(getActivity(), 0);
        setListAdapter(mAdapter);
        for (Contributor contributor: TRANSLATORS) {
            mAdapter.add(contributor);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("onCreateView()");
        View v = inflater.inflate(R.layout.fragment_gdl_list, null);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart()");
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Contributor contributor = mAdapter.getItem(position);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(contributor.getHtmlUrl()));
        startActivity(i);
    }
}
