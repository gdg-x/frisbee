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

package org.gdg.frisbee.android.activity;

import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.InjectView;
import butterknife.Optional;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.fragment.TaggedEventFragment;
import org.gdg.frisbee.android.view.ResizableImageView;
import org.joda.time.DateTime;

public class SpecialEventActivity extends GdgNavDrawerActivity {

    @InjectView(R.id.special_logo)
    @Optional
    ResizableImageView mLogo;

    @InjectView(R.id.special_description)
    TextView mDescription;

    protected String getTrackedViewName() {
        return getIntent().hasExtra(Const.SPECIAL_EVENT_VIEWTAG_EXTRA) ? getIntent().getStringExtra(Const.SPECIAL_EVENT_VIEWTAG_EXTRA) : "SpecialEvent";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getIntent().getIntExtra(Const.SPECIAL_EVENT_LAYOUT_EXTRA, R.layout.activity_special));

        getSupportActionBar().setLogo(getIntent().getIntExtra(Const.SPECIAL_EVENT_LOGO_EXTRA, R.drawable.ic_logo_devfest));

        if (mDescription != null) {
            mDescription.setText(getString(getIntent().getIntExtra(Const.SPECIAL_EVENT_DESCRIPTION_EXTRA, R.string.special_description)));
        }

        if (mLogo != null) {
            mLogo.setImageResource(getIntent().getIntExtra(Const.SPECIAL_EVENT_LOGO_EXTRA, R.drawable.ic_logo_devfest));
        }

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.content_fragment, TaggedEventFragment.newInstance(
                getIntent().hasExtra(Const.SPECIAL_EVENT_CACHEKEY_EXTRA) ? getIntent().getStringExtra(Const.SPECIAL_EVENT_CACHEKEY_EXTRA) : "specialevent",
                getIntent().hasExtra(Const.SPECIAL_EVENT_VIEWTAG_EXTRA) ? getIntent().getStringExtra(Const.SPECIAL_EVENT_VIEWTAG_EXTRA) : "specialevent",
                getIntent().getLongExtra(Const.SPECIAL_EVENT_START_EXTRA, DateTime.now().getMillis()),
                getIntent().getLongExtra(Const.SPECIAL_EVENT_END_EXTRA, DateTime.now().getMillis() + 604800000),
                getIntent().getIntExtra(Const.SPECIAL_EVENT_FRAGMENT_LAYOUT_EXTRA, R.layout.fragment_events)));
        trans.commit();
    }

}
