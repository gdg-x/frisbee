package org.gdg.frisbee.android.api.model;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 07.07.13
 * Time: 20:36
 * To change this template use File | Settings | File Templates.
 */
public class GdlShowList {

    private String mPagingCursor = null;
    private ArrayList<GdlShow> mShows;

    public GdlShowList(String pagingCursor) {
        super();
        mShows = new ArrayList<GdlShow>();
        mPagingCursor = pagingCursor;
    }

    public ArrayList<GdlShow> getShows() {
        return mShows;
    }

    public void setShows(ArrayList<GdlShow> shows) {
        mShows = shows;
    }

    public String getPagingCursor() {
        return mPagingCursor;
    }

    public void setPagingCursor(String mPagingCursor) {
        this.mPagingCursor = mPagingCursor;
    }
}
