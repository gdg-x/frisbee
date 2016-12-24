package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.Toolbar;

import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;

@RunWith(AndroidJUnit4.class)
public class MainActivityDeepLinkTest {

    private static final Uri URI_GDG_BRUSSELS = Uri.parse("https://developers.google.com/groups/chapter/105068877693379070381/");

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<MainActivity>(MainActivity.class, true, false) {
        @Override
        protected void beforeActivityLaunched() {
            PrefUtils.setInitialSettings(InstrumentationRegistry.getTargetContext(), false);
        }
    };

    @Test
    public void canHandleDevelopersGoogleChapterUri() {
        activityRule.launchActivity(new Intent(Intent.ACTION_VIEW, URI_GDG_BRUSSELS));
        onViewChapterSwitcher().check(matches(withText("GDG Brussels")));
    }

    @Test
    public void doesNotFailWithInvalidUri() {
        activityRule.launchActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://developers.google.com/groups/chapter")));
        onViewChapterSwitcher().check(matches(withText(any(String.class))));
    }

    private static ViewInteraction onViewChapterSwitcher() {
        return onView(allOf(withId(android.R.id.text1), isDescendantOfA(isAssignableFrom(Toolbar.class))));
    }
}
