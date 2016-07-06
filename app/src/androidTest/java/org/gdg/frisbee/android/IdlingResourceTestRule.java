package org.gdg.frisbee.android;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingPolicies;

import org.gdg.frisbee.android.app.App;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.concurrent.TimeUnit;

public class IdlingResourceTestRule extends TestWatcher {

    private OkHttp3IdlingResource idlingResource;

    @Override
    protected void starting(Description description) {
        IdlingPolicies.setMasterPolicyTimeout(2, TimeUnit.MINUTES);
        IdlingPolicies.setIdlingResourceTimeout(2, TimeUnit.MINUTES);

        App app = (App) InstrumentationRegistry.getTargetContext().getApplicationContext();
        idlingResource = OkHttp3IdlingResource.create("OkHttp", app.getOkHttpClient());
        Espresso.registerIdlingResources(idlingResource);
    }

    @Override
    protected void finished(Description description) {
        Espresso.unregisterIdlingResources(idlingResource);
    }

}
