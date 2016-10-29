package org.gdg.frisbee.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.assertj.core.api.Condition;
import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.onboarding.StartActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class IntentFilterTest {

    private static final Uri URI_GDG_BRUSSELS = Uri.parse("https://developers.google.com/groups/chapter/105068877693379070381/");

    @Test
    public void developerGroupsLaunchesMainActivity() throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(URI_GDG_BRUSSELS);
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(ofType(StartActivity.class));
    }

    @Test
    public void mainActivityDoesNotLaunchWithInvalidUri() throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https"));
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areNot(ofType(StartActivity.class));
    }

    @Test
    public void gdgroupsEventsLinkWithoutIdLaunchesParseDeepLinkActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Const.URL_GDGROUPS_ORG + "/" + Const.PATH_GDGROUPS_ORG_EVENT + "/"));
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(ofType(ParseDeepLinkActivity.class));
    }

    @Test
    public void gdgroupsEventsLinkLaunchesParseDeepLinkActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Const.URL_GDGROUPS_ORG + "/" + Const.PATH_GDGROUPS_ORG_EVENT + "/xyz"));
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(ofType(ParseDeepLinkActivity.class));
    }

    @NonNull
    private static Condition<ResolveInfo> ofType(final Class<? extends Activity> activityClass) {
        return new Condition<ResolveInfo>() {
            @Override
            public boolean matches(final ResolveInfo value) {
                return activityClass.getName().equals(value.activityInfo.name);
            }
        };
    }
}
