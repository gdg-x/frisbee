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

/**
 * GDG Aachen
 * org.gdg.frisbee.android.graphics.drawable
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 17:19
 */
public class VignetteDrawable extends RoundCornerDrawable {
    private boolean mUseGradientOverlay;


    public VignetteDrawable(Bitmap bitmap, float cornerRadius, int margin) {
        super(bitmap, cornerRadius, margin);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        int[] colors = new int[] { 0,0, 0x7f000000 };
        float[] pos = new float[] { 0.0f, 0.7f, 1.0f };

        RadialGradient vignette = new RadialGradient(getRect().centerX(), getRect().centerY() * 1.0f / 0.7f, getRect().centerX() * 1.3f, colors, pos, Shader.TileMode.CLAMP);
        Matrix oval = new Matrix();
        oval.setScale(1.0f, 0.7f);
        vignette.setLocalMatrix(oval);

        getPaint().setShader(new ComposeShader(getBitmapShader(), vignette, PorterDuff.Mode.SRC_OVER));
    }
}
