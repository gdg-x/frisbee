package org.gdg.frisbee.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Filter;
import android.widget.ListView;

public class FilterListView extends ListView {
    private Filter.FilterListener mFilterListener;

    public FilterListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFilterComplete(int count) {
        super.onFilterComplete(count);
        if (mFilterListener != null) {
            mFilterListener.onFilterComplete(count);
        }
    }

    public void setFilterListener(Filter.FilterListener filterListener) {
        mFilterListener = filterListener;
    }
}
