package org.gdg.frisbee.android.api.model;

import android.os.Parcel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ChapterTest {

    @Test
    public void testEquals(){
        Chapter c1 = new Chapter();
        Chapter c2 = new Chapter();
        assert(c1.equals(c2));

        c1 = new Chapter("GDG Test", "0000000000000000");
        c2 = new Chapter("GDG Test", "0000000000000000");
        assert(c1.equals(c2));

        c1 = new Chapter("GDG Test", "0000000000000000");
        c2 = new Chapter("GDG Test", "1111111111111111");
        assert(!c1.equals(c2));
    }

    @Test
    public void testWriteToParcel() {
        Chapter c1 = new Chapter("GDG Test", "0000000000000000");
        Parcel parcel = Parcel.obtain();
        c1.writeToParcel(parcel, 0);

        Chapter c2 = new Chapter(parcel);
        assert(c1.equals(c2));
    }
}
