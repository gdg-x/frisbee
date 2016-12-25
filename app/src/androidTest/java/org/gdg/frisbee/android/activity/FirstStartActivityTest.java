package org.gdg.frisbee.android.activity;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.onboarding.FirstStartActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FirstStartActivityTest {
    @Rule
    public ActivityTestRule<FirstStartActivity> rule = new ActivityTestRule<>(FirstStartActivity.class);

    @Test
    public void activityStarts() {
        onView(withText(R.string.welcome)).check(matches(isDisplayed()));
    }
}
