package org.gdg.frisbee.android.api;

import java.util.ArrayList;

public class PagedList<T> {
    int count;
    int pages;
    int pageIndex;
    int perPages;
    ArrayList<T> items;

    public ArrayList<T> getItems() {
        return items;
    }
}
