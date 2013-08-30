package org.gdg.frisbee.android.test;

import android.test.ActivityInstrumentationTestCase2;
import com.jayway.android.robotium.solo.Solo;
import org.gdg.frisbee.android.activity.MainActivity;

/**
 * @author maui
 * @since 2013-06-28 15:48
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testActivityAppears(){
        solo.assertCurrentActivity("Correct activity did not appear.", MainActivity.class);
    }
}
