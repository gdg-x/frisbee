/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.v4.content.ContextCompat;

import com.squareup.picasso.Transformation;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.utils.Utils;

public class CircularTransformation implements Transformation {

    @ColorInt
    private final int borderColor;
    @Dimension
    private final int borderRadius;

    public static CircularTransformation createWithBorder(Context context) {
        return new CircularTransformation(
            ContextCompat.getColor(context, R.color.white),
            Utils.dpToPx(context.getResources(), 1)
        );
    }

    public static CircularTransformation createBorderless() {
        return new CircularTransformation(0, 0);
    }

    private CircularTransformation(@ColorInt int borderColor, @Dimension int borderRadius) {
        this.borderColor = borderColor;
        this.borderRadius = borderRadius;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int minEdge = Math.min(source.getWidth(), source.getHeight());
        float radius = minEdge / 2f;
        int dx = (source.getWidth() - minEdge) / 2;
        int dy = (source.getHeight() - minEdge) / 2;

        // Create the canvas
        Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap output = Bitmap.createBitmap(minEdge, minEdge, config);
        Canvas canvas = new Canvas(output);

        // Init paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (borderRadius != 0) {
            // Draw background with border
            paint.setColor(borderColor);
            canvas.drawCircle(radius, radius, radius, paint);
        }

        // Init shader from the source
        Shader shader = new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix matrix = new Matrix();
        matrix.setTranslate(-dx, -dy);   // Move the target area to center of the source bitmap
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);

        // Draw the original source on top as a circle
        canvas.drawCircle(radius, radius, radius - borderRadius, paint);

        // Recycle the source bitmap, because we already generate a new one
        source.recycle();

        return output;
    }

    @Override
    public String key() {
        return "circular with borderRadius=" + borderRadius + " and borderColor=" + borderColor;
    }
}
