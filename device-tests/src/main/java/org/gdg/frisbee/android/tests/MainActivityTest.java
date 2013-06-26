package org.gdg.frisbee.android.tests;

import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.activity.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    public void test_should_show_three_pages(){
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.pager);
        assertEquals(3, viewPager.getAdapter().getCount());
    }
}
