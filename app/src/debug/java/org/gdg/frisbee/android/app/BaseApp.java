package org.gdg.frisbee.android.app;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

public class BaseApp extends MultiDexApplication {

    protected void onAppUpdate(int oldVersion, int newVersion) {
    }
}
