package org.gdg.frisbee.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.R;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 08.07.13
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */
public class PullToRefreshTransformer extends PullToRefreshAttacher.HeaderTransformer {
    private TextView mHeaderTextView;
    private ProgressBar mHeaderProgressBar;

    private CharSequence mPullRefreshLabel, mRefreshingLabel, mReleaseLabel;

    private final Interpolator mInterpolator = new AccelerateInterpolator();

    @Override
    public void onViewCreated(Activity activity, View headerView) {
        // Get ProgressBar and TextView. Also set initial text on TextView
        mHeaderProgressBar = (ProgressBar) headerView.findViewById(R.id.ptr_progress);
        mHeaderTextView = (TextView)headerView.findViewById(R.id.ptr_text);

        // Labels to display
        mPullRefreshLabel = activity.getString(R.string.pull_to_refresh_pull_label);
        mRefreshingLabel = activity.getString(R.string.pull_to_refresh_refreshing_label);
        mReleaseLabel = activity.getString(R.string.pull_to_refresh_release_label);

        // Call onReset to make sure that the View is consistent
        onReset();
    }

    @Override
    public void onReset() {
        // Reset Progress Bar
        if (mHeaderProgressBar != null) {
            mHeaderProgressBar.setVisibility(View.GONE);
            mHeaderProgressBar.setProgress(0);
            mHeaderProgressBar.setIndeterminate(false);
        }

        // Reset Text View
        if (mHeaderTextView != null) {
            mHeaderTextView.setVisibility(View.VISIBLE);
            mHeaderTextView.setText(mPullRefreshLabel);
        }
    }

    @Override
    public void onPulled(float percentagePulled) {
        if (mHeaderProgressBar != null) {
            mHeaderProgressBar.setVisibility(View.VISIBLE);
            final float progress = mInterpolator.getInterpolation(percentagePulled);
            mHeaderProgressBar.setProgress(Math.round(mHeaderProgressBar.getMax() * progress));
        }
    }

    @Override
    public void onRefreshStarted() {
        if (mHeaderTextView != null) {
            mHeaderTextView.setText(mRefreshingLabel);
        }
        if (mHeaderProgressBar != null) {
            mHeaderProgressBar.setVisibility(View.VISIBLE);
            mHeaderProgressBar.setIndeterminate(true);
        }
    }

    @Override
    public void onReleaseToRefresh() {
        if (mHeaderTextView != null) {
            mHeaderTextView.setText(mReleaseLabel);
        }
        if (mHeaderProgressBar != null) {
            mHeaderProgressBar.setProgress(mHeaderProgressBar.getMax());
        }
    }

    /**
     * Set Text to show to prompt the user is pull (or keep pulling).
     * @param pullText - Text to display.
     */
    public void setPullText(CharSequence pullText) {
        mPullRefreshLabel = pullText;
        if (mHeaderTextView != null) {
            mHeaderTextView.setText(mPullRefreshLabel);
        }
    }

    /**
     * Set Text to show to tell the user that a refresh is currently in progress.
     * @param refreshingText - Text to display.
     */
    public void setRefreshingText(CharSequence refreshingText) {
        mRefreshingLabel = refreshingText;
    }

    /**
     * Set Text to show to tell the user has scrolled enough to refresh.
     * @param releaseText - Text to display.
     */
    public void setReleaseText(CharSequence releaseText) {
        mReleaseLabel = releaseText;
    }

    protected Drawable getActionBarBackground(Context context) {
        int[] android_styleable_ActionBar = { android.R.attr.background };

        // Need to get resource id of style pointed to from actionBarStyle
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.actionBarStyle, outValue, true);
        // Now get action bar style values...
        TypedArray abStyle = context.getTheme().obtainStyledAttributes(outValue.resourceId,
                android_styleable_ActionBar);
        try {
            // background is the first attr in the array above so it's index is 0.
            return abStyle.getDrawable(0);
        } finally {
            abStyle.recycle();
        }
    }

    protected int getActionBarSize(Context context) {
        int[] attrs = { android.R.attr.actionBarSize };
        TypedArray values = context.getTheme().obtainStyledAttributes(attrs);
        try {
            return values.getDimensionPixelSize(0, 0);
        } finally {
            values.recycle();
        }
    }
}