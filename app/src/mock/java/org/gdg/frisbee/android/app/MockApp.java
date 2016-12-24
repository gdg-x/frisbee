package org.gdg.frisbee.android.app;

import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.MockGdgXHub;
import org.gdg.frisbee.android.utils.PrefUtils;

public class MockApp extends App {

    @Override
    public void onCreate() {
        super.onCreate();
        PrefUtils.skipSeasonsGreetings(this);
    }

    @Override
    public GdgXHub getGdgXHub() {
        return new MockGdgXHub(this);
    }

}
