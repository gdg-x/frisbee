package org.gdg.frisbee.android.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

public class BaseApp extends Application {

    protected void onAppUpdate(int oldVersion, int newVersion) {
    }
}
