package org.gdg.frisbee.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import org.assertj.core.api.Condition;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class IntentFilterTest {

    @Test
    public void developerGroupsLaunchesMainActivity() throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(MainActivityDeepLinkTest.URI_GDG_BRUSSELS);
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(ofType(MainActivity.class));
    }

    @Test
    public void mainActivityDoesNotLaunchWithInvalidUri() throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https"));
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areNot(ofType(MainActivity.class));
    }

    @Test
    public void gdgroupEventsLinkWithoutIdLaunchesMainActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Const.URL_GDGROUPS_ORG + "/" + Const.PATH_GDGROUPS_ORG_EVENT));
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(ofType(MainActivity.class));
    }

    @Test
    public void gdgroupEventsLinkLaunchesParseDeepLinkActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Const.URL_GDGROUPS_ORG + "/" + Const.PATH_GDGROUPS_ORG_EVENT + "/xyz"));
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(ofType(ParseDeepLinkActivity.class));
    }

    @NonNull
    private static Condition<ResolveInfo> ofType(final Class<? extends Activity> activitiyClass) {
        return new Condition<ResolveInfo>() {
            @Override
            public boolean matches(final ResolveInfo value) {
                return activitiyClass.getName().equals(value.activityInfo.name);
            }
        };
    }
}
