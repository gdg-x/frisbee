package org.gdg.frisbee.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.view
 * <p/>
 * User: maui
 * Date: 14.06.13
 * Time: 03:24
 */
public class ResizableImageView extends ImageView {

    private int mWidth = -1, mHeight = -1;

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdjustViewBounds(true);
    }

    public void setDimensions(long width, long height) {
        mWidth = (int) width;
        mHeight = (int) height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Drawable d = getBackground();

        if (d == null) {
            d = getDrawable();
        }

        if (d != null && d.getIntrinsicWidth() != -1) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * d.getIntrinsicHeight() / d.getIntrinsicWidth();
            setMeasuredDimension(width, height);
        } else if (mWidth != -1) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            float ratio = (float) mHeight / (float) mWidth;
            int height = (int) (width * ratio);

            //Timber.d("Measured Width: "+ width+", Computed Height: "+ height+", Pic W: "+ mWidth+", H: "+ mHeight+", Ratio: "+ ratio +", "+mUri);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
