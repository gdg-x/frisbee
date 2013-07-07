package org.gdg.frisbee.android.api.model;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 20:36
 * To change this template use File | Settings | File Templates.
 */
public class GdlShowList extends ArrayList<GdlShow> {

    private String mPagingCursor = null;

    public GdlShowList(String pagingCursor) {
        super();
        mPagingCursor = pagingCursor;
    }

    public String getPagingCursor() {
        return mPagingCursor;
    }

    public void setPagingCursor(String mPagingCursor) {
        this.mPagingCursor = mPagingCursor;
    }
}
