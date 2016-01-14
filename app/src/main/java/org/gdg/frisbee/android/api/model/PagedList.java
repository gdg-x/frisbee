package org.gdg.frisbee.android.api.model;

import java.util.ArrayList;

public class PagedList<T> {
    int count;
    int pages;
    int pageIndex;
    int perPages;
    ArrayList<T> items;

    public PagedList() {
        this.items = new ArrayList<>();
    }

    public ArrayList<T> getItems() {
        return items;
    }
}
