package org.gdg.frisbee.android.app;

import android.app.Application;
import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 22.08.13
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
public class TestApp extends Application implements TestLifecycleApplication {
    @Override
    public void beforeTest(Method method) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void prepareTest(Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterTest(Method method) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
