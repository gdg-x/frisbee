package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.test.InstrumentationTestCase;

import org.assertj.core.api.Condition;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class IntentFilterTest extends InstrumentationTestCase {

    public void testDeveloperGroupsLaunchesMainActivity() throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://developers.google.com/groups/chapter/105068877693379070381/"));
        final List<ResolveInfo> activities = getInstrumentation().getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(new Condition<ResolveInfo>() {
            @Override
            public boolean matches(final ResolveInfo value) {
                return MainActivity.class.getName().equals(value.activityInfo.name);
            }
        });
    }
}
