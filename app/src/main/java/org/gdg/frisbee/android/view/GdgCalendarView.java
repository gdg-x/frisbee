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
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import org.gdg.frisbee.android.utils.Utils;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.view
 * <p/>
 * User: maui
 * Date: 22.04.13
 * Time: 23:41
 */
public class GdgCalendarView extends CalendarView {

    public GdgCalendarView(Context context) {
        super(context);
    }

    public GdgCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GdgCalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private static Class mWeekView, mCalendarView;
    private static Field mDrawPaintField, mTextSizeField;

    private void workaround34932() {
        Log.d("CalendarView", "Executing Workaround for Bug #34932");
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
            try {
                if(mWeekView == null) {
                    mWeekView = Class.forName("android.widget.CalendarView").getDeclaredClasses()[0];
                    mCalendarView = Class.forName("android.widget.CalendarView");
                    mDrawPaintField = mWeekView.getDeclaredField("mMonthNumDrawPaint");
                    mTextSizeField = mCalendarView.getDeclaredField("mDateTextSize");
                    mDrawPaintField.setAccessible(true);
                    mTextSizeField.setAccessible(true);
                }
                ArrayList<View> views = Utils.findViewByType(this, mWeekView);
                Integer textSize = (Integer) mTextSizeField.get(this);
                for(View v : views) {
                    Paint paint = (Paint) mDrawPaintField.get(v);
                    paint.setTextSize(textSize);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        workaround34932();
    }
}
