package org.gdg.frisbee.android.activity;

import android.os.Bundle;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.app.App;

public class DevFestActivity extends GdgNavDrawerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devfest);

        getSupportActionBar().setLogo(R.drawable.ic_logo_devfest);
        App.getInstance().getTracker().sendView("/DevFest");
    }
}
