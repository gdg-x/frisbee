package org.gdg.frisbee.android.api;

import android.support.test.espresso.IdlingResource;

public class EspressoIdlingResource {

    private static NetworkIdlingResource mCountingIdlingResource =
        new NetworkIdlingResource();

    private EspressoIdlingResource() {
    }

    public static void increment() {
        mCountingIdlingResource.increment();
    }

    public static void decrement() {
        mCountingIdlingResource.decrement();
    }

    public static IdlingResource getIdlingResource() {
        return mCountingIdlingResource;
    }
}
