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
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 08.07.13
 * Time: 01:49
 * To change this template use File | Settings | File Templates.
 */
public class ExtLibrariesFragment extends Fragment {

    @InjectView(R.id.external)
    TextView mExternal;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_external_libraries, null);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.

        InputStream is = null;
        try {
            is = getResources().getAssets().open("third_party.html");
            mExternal.setText(Html.fromHtml(Utils.inputStreamToString(is)));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}
