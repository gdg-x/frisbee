package org.gdg.frisbee.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.view
 * <p/>
 * User: maui
 * Date: 14.06.13
 * Time: 03:24
 */
public class ResizableImageView extends ImageView {

    private static final String LOG_TAG = "GDG-ResizableImageView";

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdjustViewBounds(true);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

        Drawable d = getBackground();

        if(d == null)
            d = getDrawable();

        if(d != null && d.getIntrinsicWidth() != -1){
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * d.getIntrinsicHeight() / d.getIntrinsicWidth();
            setMeasuredDimension(width, height);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}