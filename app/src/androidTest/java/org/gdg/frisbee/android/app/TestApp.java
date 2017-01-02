package org.gdg.frisbee.android.app;


import org.gdg.frisbee.android.utils.PrefUtils;

public class TestApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();

        PrefUtils.setInitialSettings(this, false);
        PrefUtils.skipSeasonsGreetings(this);
    }
}
