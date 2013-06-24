package org.gdg.frisbee.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
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

    private static final String LOG_TAG = "GDG-ResizableImageView";

    public ResizableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){

        Drawable d = getDrawable();

        if(d!=null){
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = width * d.getIntrinsicHeight() / d.getIntrinsicWidth();
            setMeasuredDimension(width, height);
        }else{
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        if(drawable != null)
            Log.d(LOG_TAG, "setImageDrawable");

        super.setImageDrawable(drawable);
        requestLayout();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm != null)
            Log.d(LOG_TAG, "setImageBitmap");

        super.setImageBitmap(bm);
        requestLayout();
    }
}