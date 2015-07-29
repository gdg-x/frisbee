package org.gdg.frisbee.android.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.assertj.core.api.Condition;
import org.gdg.frisbee.android.chapter.MainActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class IntentFilterTest {

    @Test
    public void developerGroupsLaunchesMainActivity() throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(MainActivityDeepLinkTest.URI_GDG_BRUSSELS);
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areAtLeastOne(new Condition<ResolveInfo>() {
            @Override
            public boolean matches(final ResolveInfo value) {
                return MainActivity.class.getName().equals(value.activityInfo.name);
            }
        });
    }

    @Test
    public void mainActivityDoesNotLaunchWithInvalidUri() throws PackageManager.NameNotFoundException {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https"));
        final List<ResolveInfo> activities = InstrumentationRegistry.getTargetContext().getPackageManager().queryIntentActivities(intent, 0);

        assertThat(activities).areNot(new Condition<ResolveInfo>() {
            @Override
            public boolean matches(final ResolveInfo value) {
                return MainActivity.class.getName().equals(value.activityInfo.name);
            }
        });
    }
}
