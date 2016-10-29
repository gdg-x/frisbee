package org.gdg.frisbee.android.app;

import org.gdg.frisbee.android.api.GdgXHub;
import org.gdg.frisbee.android.api.MockGdgXHub;

public class MockApp extends App {

    @Override
    public GdgXHub getGdgXHub() {
        return new MockGdgXHub(this);
    }

}
