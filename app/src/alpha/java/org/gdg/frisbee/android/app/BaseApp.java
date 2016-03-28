package org.gdg.frisbee.android.app;

import android.app.Application;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.gdg.frisbee.android.utils.FileUtils;
import org.gdg.frisbee.android.utils.PrefUtils;

import java.io.File;

public class BaseApp extends Application {

    protected void onAppUpdate(int oldVersion, int newVersion) {
        PrefUtils.resetInitialSettings(this);

        deleteCacheDirs();

        Toast.makeText(getApplicationContext(), "Alpha version always resets Preferences on update.", Toast.LENGTH_LONG).show();
    }

    private void deleteCacheDirs() {
        File[] cacheDirs = ContextCompat.getExternalCacheDirs(this);
        for (File cacheDir : cacheDirs) {
            if (cacheDir != null) {
                FileUtils.deleteDirectory(cacheDir);
            }
        }
        FileUtils.deleteDirectory(getCacheDir());
    }

}
