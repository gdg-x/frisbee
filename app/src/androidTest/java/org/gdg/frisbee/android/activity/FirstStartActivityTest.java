package org.gdg.frisbee.android.activity;

import android.support.v4.view.ViewPager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Spinner;

import com.robotium.solo.Solo;

import org.gdg.frisbee.android.R;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 28.06.13
 * Time: 17:02
 * To change this template use File | Settings | File Templates.
 */
public class FirstStartActivityTest extends ActivityInstrumentationTestCase2<FirstStartActivity> {

    private Solo solo;

    public FirstStartActivityTest() {
        super(FirstStartActivity.class);
    }

    @Override
    protected void setUp() throws Exception {

        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testActivityAppears() {
        solo.assertCurrentActivity("Correct activity did not appear.", FirstStartActivity.class);
    }

    public void testChapterList() {
        solo.waitForActivity(FirstStartActivity.class);

        Spinner chapterList = (Spinner) solo.getView(R.id.chapter_spinner);

        assertNotNull("Chapter Spinner has no Adapter", chapterList.getAdapter());
        assertFalse("Chapter list is empty", chapterList.getAdapter().getCount() == 0);

        solo.pressSpinnerItem(0, 0);
        assertEquals("Blub", true, solo.isSpinnerTextSelected(0, "6th October"));
    }

    public void testMoveStep2() {

        solo.clickOnButton(0);

        ViewPager vp = (ViewPager) solo.getView(R.id.pager);

        assertTrue("Pager did not move to step2", vp.getCurrentItem() == 1);
    }

    public void testFinishFirstStart() {
        solo.clickOnButton(0);
        solo.clickOnButton(2);
    }
}
