package org.gdg.frisbee.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.plus.PlusShare;

public class ParseDeepLinkActivity extends Activity {

    public static final String EVENTS = "events";

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
        String[] parts = deepLinkId.split("/");
        if (parts.length == 3 && EVENTS.equals(parts[1])) {
            route.setClass(getApplicationContext(), MainActivity.class);
            route.putExtra(MainActivity.EXTRA_GROUP_ID, parts[0]);
            route.putExtra(MainActivity.EXTRA_SECTION, MainActivity.SECTION_EVENTS);
        } else {
            // Fallback to the MainActivity in your app.
            route.setClass(getApplicationContext(), MainActivity.class);
        }
        return route;
    }
}
