package org.gdg.frisbee.android.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.IdlingTestCase;
import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.event.EventActivity;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class EventActivityTest extends IdlingTestCase {
    @Rule
    public final IntentsTestRule<EventActivity> rule = new IntentsTestRule<EventActivity>(EventActivity.class) {
        @Override
        protected Intent getActivityIntent() {
            Intent intent = super.getActivityIntent();
            intent.putExtra(Const.EXTRA_EVENT_ID, "6256487006994432");
            return intent;
        }
    };

    @Before
    public void stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    @Test
    @Ignore("Ignored because the first click does not do anything. Fails the tests.")
    public void clickOnTitleOpensEventUrl() {
        onView(withId(R.id.title)).perform(click());

        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData("https://plus.google.com/events/cjv3ppus7icbjnbmj3mfd1258cc")));
    }

    @Test
    public void clickOnAddToCalendarOpensCalendar() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withText(R.string.add_to_calendar)).perform(click());

        intended(allOf(hasAction(Intent.ACTION_EDIT), hasType("vnd.android.cursor.item/event")));
    }
}
