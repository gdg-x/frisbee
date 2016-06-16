package org.gdg.frisbee.android.gde;


import org.gdg.frisbee.android.api.model.GdeList;

final class GdeCategory {

    private final String category;
    private final GdeList gdeList;

    GdeCategory(String category, GdeList gdeList) {
        this.category = category;
        this.gdeList = gdeList;
    }

    String getCategory() {
        return category;
    }

    GdeList getGdeList() {
        return gdeList;
    }
}
