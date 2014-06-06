package org.gdg.frisbee.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.plus.PlusShare;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.event.EventActivity;

import timber.log.Timber;

public class ParseDeepLinkActivity extends Activity {

    public static final String LOG_TAG = "GDG-ParseDeepLinkActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    String deepLinkId = PlusShare.getDeepLinkId(this.getIntent());
    Intent target = parseDeepLinkId(deepLinkId);
    if(target!=null)

    {
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
        Intent route = new Intent();
        Timber.d("Deep Link id: " + deepLinkId);
        String[] parts = deepLinkId.split("/");

        App.getInstance().getTracker().sendEvent("gplus", "deepLink", deepLinkId, 0L);

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
