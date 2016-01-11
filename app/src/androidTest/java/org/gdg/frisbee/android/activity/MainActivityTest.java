package org.gdg.frisbee.android.activity;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.utils.PrefUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.gdg.frisbee.android.activity.util.OrientationChangeAction.orientationLandscape;
import static org.gdg.frisbee.android.activity.util.OrientationChangeAction.orientationPortrait;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    public static final Chapter CHAPTER_ISTANBUL = new Chapter("Istanbul", "100514812580249787371");
    public static final Chapter CHAPTER_BRUSSELS = new Chapter("Brussels", "105068877693379070381");

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            PrefUtils.setInitialSettings(InstrumentationRegistry.getTargetContext(), false, false, null, null);
            PrefUtils.setHomeChapter(InstrumentationRegistry.getTargetContext(), CHAPTER_BRUSSELS);
        }
    };

    @Test
    public void supportsChapterSwapping() {

        onView(withId(R.id.actionbar_spinner)).perform(click());
        onData(allOf(is(instanceOf(Chapter.class)), is(CHAPTER_ISTANBUL)))
                .perform(click());
        onView(withId(R.id.actionbar_spinner))
                .check(matches(withSpinnerText(CHAPTER_ISTANBUL.toString())));

        onView(withId(R.id.pager)).perform(swipeRight());
        onView(withId(R.id.tagline)).check(matches(withText(containsString(CHAPTER_ISTANBUL.toString()))));
        onView(withId(R.id.about)).check(matches(withText(containsString(CHAPTER_ISTANBUL.toString()))));
        onView(withId(R.id.pager)).perform(swipeLeft());
    }


    @Test
    public void keepsChapterOnOrientationChange() {

        onView(isRoot()).perform(orientationPortrait());

        onView(withId(R.id.actionbar_spinner)).perform(click());
        onData(allOf(is(instanceOf(Chapter.class)), is(CHAPTER_ISTANBUL)))
                .perform(click());
        onView(withId(R.id.actionbar_spinner))
                .check(matches(withSpinnerText(CHAPTER_ISTANBUL.toString())));


        onView(isRoot()).perform(orientationLandscape());

        onView(withId(R.id.actionbar_spinner))
                .check(matches(withSpinnerText(CHAPTER_ISTANBUL.toString())));
    }
}
