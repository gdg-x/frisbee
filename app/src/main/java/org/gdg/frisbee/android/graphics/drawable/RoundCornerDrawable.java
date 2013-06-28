/*
 * Copyright 2013 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.graphics.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.graphics.drawable
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 17:21
 */
public class RoundCornerDrawable extends Drawable {

    private float mCornerRadius;
    private Paint mPaint;
    private int mMargin;
    private BitmapShader mBitmapShader;
    private RectF mRect = new RectF();

    public RoundCornerDrawable(Bitmap bitmap, float cornerRadius, int margin) {
        mCornerRadius = cornerRadius;
        mMargin = margin;
        mBitmapShader = new BitmapShader (bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(mBitmapShader);
    }

    public RectF getRect() {
        return mRect;
    }

    public BitmapShader getBitmapShader() {
        return mBitmapShader;
    }

    public Paint getPaint() {
        return mPaint;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRect.set(mMargin, mMargin, bounds.width() - mMargin, bounds.height() - mMargin);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
    }

    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
