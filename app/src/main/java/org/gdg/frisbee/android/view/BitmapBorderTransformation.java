package org.gdg.frisbee.android.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import com.squareup.picasso.Transformation;

public class BitmapBorderTransformation implements Transformation {
    private int mBorderSize;
    private int mCornerRadius = 0;
    private int mColor;

    public BitmapBorderTransformation(int borderSize, int cornerRadius, int color) {
        this.mBorderSize = borderSize;
        this.mCornerRadius = cornerRadius;
        this.mColor = color;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap source) {
        if (source.getConfig() == null) {
            return source;
        }

        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap image = Bitmap.createBitmap(width, height, source.getConfig());
        Canvas canvas = new Canvas(image);
        canvas.drawARGB(0, 0, 0, 0);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect = new Rect(0, 0, width, height);


        if (this.mCornerRadius == 0) {
            canvas.drawRect(rect, paint);
        } else {
            canvas.drawRoundRect(new RectF(rect),
                    this.mCornerRadius, this.mCornerRadius, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, rect, rect, paint);

        Bitmap output;

        if (this.mBorderSize == 0) {
            output = image;
        } else {
            width = width + this.mBorderSize * 2;
            height = height + this.mBorderSize * 2;

            output = Bitmap.createBitmap(width, height, source.getConfig());
            canvas.setBitmap(output);
            canvas.drawARGB(0, 0, 0, 0);

            rect = new Rect(0, 0, width, height);

            paint.setXfermode(null);
            paint.setColor(this.mColor);
            paint.setStyle(Paint.Style.FILL);

            canvas.drawRoundRect(new RectF(rect), this.mCornerRadius, this.mCornerRadius, paint);

            canvas.drawBitmap(image, this.mBorderSize, this.mBorderSize, null);
        }

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @NonNull
    @Override
    public String key() {
        return "bitmapBorder("
                + "borderSize="
                + this.mBorderSize + ", "
                + "cornerRadius=" + this.mCornerRadius + ", "
                + "color=" + this.mColor + ")";
    }
}
