package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.any;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public static final Uri URI_GDG_BRUSSELS = Uri.parse("https://developers.google.com/groups/chapter/105068877693379070381/");
    @Rule
    public ActivityTestRule activityRule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Test
    public void canHandleDevelopersGoogleChapterUri() {
        final Intent intent = new Intent(Intent.ACTION_VIEW, URI_GDG_BRUSSELS);
        intent.setClass(InstrumentationRegistry.getTargetContext(), MainActivity.class);
        activityRule.launchActivity(intent);
        onView(withId(R.id.actionbar_spinner)).check(matches(withSpinnerText("Brussels")));
    }

    @Test
    public void doesNotFailWithInvalidUri() {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.google.com/groups/chapter"));
        intent.setClass(InstrumentationRegistry.getTargetContext(), MainActivity.class);
        activityRule.launchActivity(intent);
        onView(withId(R.id.actionbar_spinner)).check(matches(withSpinnerText(any(String.class))));
    }
}
