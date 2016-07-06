package org.gdg.frisbee.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;
import android.support.test.runner.AndroidJUnit4;

import org.gdg.frisbee.android.app.App;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * Removes redundancy for setting up Idling Resources for Espresso.
 */
@RunWith(AndroidJUnit4.class)
public abstract class IdlingTestCase {

    private OkHttp3IdlingResource idlingResource;

    @Before
    public void setUp() throws Exception {
        IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(2, TimeUnit.MINUTES);

        App app = (App) InstrumentationRegistry.getTargetContext().getApplicationContext();
        idlingResource = OkHttp3IdlingResource.create("OkHttp", app.getOkHttpClient());
        Espresso.registerIdlingResources(idlingResource);
    }

    @After
    public void unregisterIdlingResource() {
        Espresso.unregisterIdlingResources(idlingResource);
    }
}
