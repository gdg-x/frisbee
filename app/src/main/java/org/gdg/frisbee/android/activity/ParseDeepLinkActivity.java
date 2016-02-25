package org.gdg.frisbee.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.plus.PlusShare;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.event.EventActivity;

import timber.log.Timber;

public class ParseDeepLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent target;

        String deepLinkId = PlusShare.getDeepLinkId(getIntent());
        if (deepLinkId != null) {
            target = parseDeepLinkId(deepLinkId);
        } else {
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                target = new Intent();
                target.setClass(getApplicationContext(), EventActivity.class);
                target.putExtra(Const.EXTRA_EVENT_ID, getIntent().getData().getLastPathSegment());
                target.putExtra(Const.EXTRA_SECTION, EventActivity.EventPagerAdapter.SECTION_OVERVIEW);
            } else {
                target = null;
            }
        }

        if (target != null) {
            startActivity(target);
        }

        finish();
    }

    /**
     * Get the intent for an activity corresponding to the deep-link ID.
     *
     * @param deepLinkId The deep-link ID to parse.
     * @return The intent corresponding to the deep-link ID.
     */
    private Intent parseDeepLinkId(String deepLinkId) {
        if (deepLinkId == null) {
            return null;
        }
        Intent route = new Intent();
        Timber.d("Deep Link id: " + deepLinkId);
        String[] parts = deepLinkId.split("/");

        App.getInstance().getTracker().send(new HitBuilders.EventBuilder()
            .setCategory("gplus")
            .setAction("deepLink")
            .setLabel(deepLinkId)
            .build());

        // Our deep links look like this: https://developers.google.com/events/<eventId>/join,
        // or <plus_id>/events/<eventId>/join    join is optional
        if (deepLinkId.startsWith(Const.URL_DEVELOPERS_GOOGLE_COM)) {
            route.setClass(getApplicationContext(), EventActivity.class);
            route.putExtra(Const.EXTRA_EVENT_ID, parts[4]);
            route.putExtra(Const.EXTRA_SECTION, EventActivity.EventPagerAdapter.SECTION_OVERVIEW);
        } else {
            // Fallback to the MainActivity in your app.
            route.setClass(getApplicationContext(), MainActivity.class);
        }
        return route;
    }
}
