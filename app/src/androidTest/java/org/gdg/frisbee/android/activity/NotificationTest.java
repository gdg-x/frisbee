package org.gdg.frisbee.android.activity;

import android.app.Notification;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.Toolbar;

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.gdg.frisbee.android.eventseries.NotificationHandler;
import org.gdg.frisbee.android.eventseries.TaggedEventSeries;
import org.hamcrest.core.AllOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class NotificationTest {
    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);
    private UiDevice mDevice;

    @Before
    public void setUp() throws Exception {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void testNotificationForEventSeries() throws InterruptedException {
        NotificationManagerCompat nm = NotificationManagerCompat.from(rule.getActivity());

        int i = 0;
        for (TaggedEventSeries series : App.getInstance().currentTaggedEventSeries()) {
            Notification notification = new NotificationHandler(rule.getActivity(), series)
                .createNotification();
            nm.notify(i, notification);

            mDevice.openNotification();
            Thread.sleep(2000);
            mDevice.findObject(By.textContains(rule.getActivity().getString(series.getTitleResId())))
                .click();
            Thread.sleep(1000);
            onView(
                AllOf.allOf(
                    withText(series.getTitleResId()),
                    withParent(isAssignableFrom(Toolbar.class)))
            ).check(matches(isDisplayed()));
        }
    }
}
