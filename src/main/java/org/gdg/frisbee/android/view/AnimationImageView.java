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

package org.gdg.frisbee.android.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.view
 * <p/>
 * User: maui
 * Date: 23.04.13
 * Time: 23:42
 */
public class AnimationImageView extends ImageView {

    public AnimationImageView(Context context) {
        super(context);
        initAnimation();
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAnimation();
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAnimation();
    }

    private void initAnimation() {
        if(getDrawable() != null && getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable frameAnimation = (AnimationDrawable) getDrawable();
            frameAnimation.start();
        }
    }
}
