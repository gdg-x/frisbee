/*
 * Copyright 2013-2015 The GDG Frisbee Project
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

package org.gdg.frisbee.android.special;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.GdgNavDrawerActivity;
import org.gdg.frisbee.android.fragment.TaggedEventFragment;
import org.gdg.frisbee.android.view.ResizableImageView;

import butterknife.InjectView;
import butterknife.Optional;

public class SpecialEventActivity extends GdgNavDrawerActivity {

    @InjectView(R.id.special_logo)
    @Optional
    ResizableImageView mLogo;

    @InjectView(R.id.special_description)
    TextView mDescription;
    
    private TaggedEvent mTaggedEvent;

    protected String getTrackedViewName() {
        return mTaggedEvent != null && !TextUtils.isEmpty(mTaggedEvent.getTag())
                ? mTaggedEvent.getTag() : "SpecialEvent";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_special);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_drawer);
        toolbar.setTitle(R.string.devfest);
        setSupportActionBar(toolbar);

        mTaggedEvent = getIntent().getParcelableExtra(Const.EXTRA_TAGGED_EVENT);
        if (mTaggedEvent == null) {
            throw new IllegalArgumentException("Special Event must be provided with " 
                    + Const.EXTRA_TAGGED_EVENT + " key as an Intent extra.");
        }
        
        getSupportActionBar().setLogo(mTaggedEvent.getLogoResId());

        mDescription.setText(mTaggedEvent.getDescriptionResId());

        if (mLogo != null) {
            mLogo.setImageResource(mTaggedEvent.getLogoResId());
        }

        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        final String cacheExtra = getIntent().getStringExtra(Const.EXTRA_TAGGED_EVENT_CACHEKEY);
        trans.replace(R.id.content_fragment, TaggedEventFragment.newInstance(
                cacheExtra != null ? cacheExtra : "specialevent",
                        mTaggedEvent));
        trans.commit();
    }

}
