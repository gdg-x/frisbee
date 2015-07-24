package org.gdg.frisbee.android.activity;

import android.test.ActivityInstrumentationTestCase2;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.onboarding.FirstStartActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class FirstActivityTest extends ActivityInstrumentationTestCase2<FirstStartActivity> {

    public FirstActivityTest() {
        super(FirstStartActivity.class);
    }

    public void testFirstStartStep1ToStep2() {
        getActivity();
        onView(withId(R.id.confirm)).perform(click());
        onView(withId(R.id.skipSignin)).check(matches(isDisplayed()));
    }
}
