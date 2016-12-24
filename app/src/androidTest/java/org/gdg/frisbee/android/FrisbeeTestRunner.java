package org.gdg.frisbee.android;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;

import org.gdg.frisbee.android.app.TestApp;

public class FrisbeeTestRunner extends AndroidJUnitRunner {

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
        throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newApplication(cl, TestApp.class.getName(), context);
    }
}
