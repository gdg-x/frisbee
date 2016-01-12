package org.gdg.frisbee.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.CompletionInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;

public class AutoCompleteSpinnerView extends AutoCompleteTextView {
    private Filter.FilterListener mFilterListener;

    public AutoCompleteSpinnerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onFilterComplete(int count) {
        super.onFilterComplete(count);
        if (mFilterListener != null) {
            mFilterListener.onFilterComplete(count);
        }
    }

    public void setFilterCompletionListener(Filter.FilterListener filterListener) {
        mFilterListener = filterListener;
    }
}
