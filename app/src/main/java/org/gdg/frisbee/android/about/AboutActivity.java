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

package org.gdg.frisbee.android.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appinvite.AppInviteInvitation;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.common.GdgActivity;

import butterknife.BindView;
import timber.log.Timber;

public class AboutActivity extends GdgActivity {

    private static final int REQUEST_INVITE = 101;

    @BindView(R.id.pager)
    ViewPager mViewPager;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        getActionBarToolbar().setTitle(R.string.about);
        getActionBarToolbar().setNavigationIcon(R.drawable.ic_up);

        mViewPager.setAdapter(new AboutPagerAdapter(getSupportFragmentManager(), getResources()));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    protected String getTrackedViewName() {
        return "About/" + getResources().getStringArray(R.array.about_tabs)[getCurrentPage()];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            finish();
            return true;
        } else if (R.id.action_app_invite == item.getItemId()) {
            Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .build();
            startActivityForResult(intent, REQUEST_INVITE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onActivityResult: requestCode= %d, resultCode= %d", requestCode, resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent and log a message
                // The ids array contains the unique invitation ids for each invitation sent
                // (one for each contact select by the user). You can use these for analytics
                // as the ID will be consistent on the sending and receiving devices.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Timber.d("Sent %d invitations", ids.length);
                sendAnalyticsEvent(
                    "AppInvite",
                    "Successful",
                    String.valueOf(ids.length),
                    ids.length);
            } else {
                // Sending failed or it was canceled, show failure message to the user
                showError(R.string.invitation_error_message);

                sendAnalyticsEvent(
                    "AppInvite",
                    "Error",
                    ""
                );
            }
        }
    }

}
