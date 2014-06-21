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
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.gdg.frisbee.android.adapter.NewsAdapter;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Map;

/**
 * GDG Aachen
 * org.gdg.frisbee.android.view
 * <p/>
 * User: maui
 * Date: 29.04.13
 * Time: 16:56
 */
public class MyView extends AbsListView {

    private final static String LOG_TAG = "GDG-MyView";

    private ListAdapter mAdapter;

    private LinearLayout mRoot;
    private ArrayList<WrapAdapter> mAdapters;
    private ArrayList<ListView> mColumns;
    private int mColumnCount = 1;

    public MyView(Context context) {
        super(context);
        mColumns = new ArrayList<ListView>();
        mAdapters = new ArrayList<WrapAdapter>();
        mColumns = new ArrayList<ListView>();
        mAdapters = new ArrayList<WrapAdapter>();
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        int columns = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto","numColumns",1);
        mColumns = new ArrayList<ListView>(columns);
        mAdapters = new ArrayList<WrapAdapter>(columns);
        mColumnCount = columns;
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int columns = attrs.getAttributeIntValue("http://schemas.android.com/apk/res-auto","numColumns",1);
        mColumns = new ArrayList<ListView>(columns);
        mAdapters = new ArrayList<WrapAdapter>(columns);
        mColumnCount = columns;
    }

    private void initLayout() {
        mRoot = new LinearLayout(getContext());
        mRoot.setOrientation(LinearLayout.HORIZONTAL);

        // Defining the LinearLayout layout parameters to fill the parent.
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mRoot.setLayoutParams(llp);

        for(int i = 0; i < mColumnCount; i++) {
            ListView list = new InnerListView(getContext());
            list.setDivider(null);
            list.setDividerHeight(0);
            list.setVerticalScrollBarEnabled(false);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                list.setScrollBarSize(0);
                //list.setScrollIndicators(null, null);
            }

            mColumns.add(list);
            LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            l.weight = 1;

            mRoot.addView(list, i, l);
            list.setAdapter(mAdapters.get(i));
        }

        addViewInLayout(mRoot, -1, llp, false);

        mRoot.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
        mRoot.layout(0,0,getWidth(),getHeight());
        Timber.d("layoutInit()");
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mAdapter = adapter;

        for(int i = 0; i < mColumnCount; i++) {
            mAdapters.add(new WrapAdapter(mAdapter));
        }

        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onInvalidated() {
                super.onInvalidated();
                Timber.d("onInvalidated()");
            }
            @Override
            public void onChanged() {
                super.onChanged();
                applyAdapterUpdate();
            }
        });
        Timber.d("setAdapter()");
    }

    /*
    @Override
    public int getChildCount() {
        if(mRoot == null)
            return 0;

        int children = 0;
        return mColumns.size();
    }

    @Override
    public View getChildAt(int index) {
        View v = null;
        ViewGroup vg = null;

        if(mRoot == null) {
            return null;
        } else {
            vg = (ViewGroup) mRoot.getChildAt(0);
            v = vg.getChildAt(index);

            if(v == null) {
                vg = (ViewGroup) mRoot.getChildAt(1);
                v = vg.getChildAt(index);
            }
        }

        if(v == null) {
            Timber.d("Not so good..."+index);
        }
        return v;
    }
*/

    private void applyAdapterUpdate() {
        for(WrapAdapter a : mAdapters) {
            a.clear();
        }

        for(int i = 0; i <mAdapter.getCount(); i++) {
            View v = mAdapter.getView(i, null, null);
            if(mAdapter instanceof NewsAdapter) {
                ((NewsAdapter)mAdapter).getItemInternal(i).setConsumed(false);
            }
            v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            getShortestAdapter().add(i, v.getMeasuredHeight());
        }
    }

    private WrapAdapter getShortestAdapter() {
        WrapAdapter a = null;
        int minHeight = Integer.MAX_VALUE;
        for(WrapAdapter adapter : mAdapters) {
            if(adapter.getHeight() < minHeight) {
                a = adapter;
                minHeight = adapter.getHeight();
            }
        }
        return a;
    }

    @Override
    public int getFirstVisiblePosition() {
        int val = 0;
        if(mRoot != null) {
            for (int i = 0; i < mColumnCount; i++) {
                ListView lv = (ListView) mRoot.getChildAt(i);
                if (lv.getFirstVisiblePosition() > val)
                    val = lv.getFirstVisiblePosition();
            }
            Timber.d("Visible: " + val);
        }
        return val;
    }

    @Override
    public ListAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setSelection(int i) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            widthMode = MeasureSpec.EXACTLY;
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            heightMode = MeasureSpec.EXACTLY;
        }

        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        initLayout();
    }

    @Override
    public void requestLayout() {
        if(mRoot != null) {
            //mRoot.requestLayout();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //return super.onInterceptTouchEvent(ev);
        return true;
    }

    @Override
    public int getCount() {
        return mAdapter.getCount();
    }

    public class WrapAdapter extends BaseAdapter {

        private ListAdapter mBaseAdapter;
        private ArrayList<Integer> mItems;
        private int mHeight = 0;

        public WrapAdapter(ListAdapter adapter) {
            mBaseAdapter = adapter;
            mItems = new ArrayList<Integer>();
        }

        public int getHeight() {
            return mHeight;
        }

        public void add(int baseItem, int height) {
            mHeight += height;
            mItems.add(baseItem);
            notifyDataSetChanged();
        }

        public void addAll(Map<Integer,Integer> items) {
            for(Map.Entry<Integer,Integer> item : items.entrySet()) {
                mHeight += item.getValue();
                mItems.add(item.getKey());
            }
            notifyDataSetChanged();
        }

        public void clear() {
            mItems.clear();
            mHeight = 0;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mBaseAdapter.getItem(mItems.get(i));
        }

        @Override
        public long getItemId(int i) {
            return mBaseAdapter.getItemId(mItems.get(i));
        }

        @Override
        public int getViewTypeCount() {
            return mBaseAdapter.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position) {
            return mBaseAdapter.getItemViewType(mItems.get(position));
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = mBaseAdapter.getView(mItems.get(i), view, viewGroup);
            return v;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*Rect hit = new Rect();
        for(ListView v : mColumns) {
            v.getDrawingRect(hit);
            if(hit.contains((int) ev.getX(), (int) ev.getY()))
                return v.onTouchEvent(ev);
        } */
        for(ListView v : mColumns) {
            v.dispatchTouchEvent(ev);
        }
        return true;
    }

    public class InnerListView extends ListView {

        public InnerListView(Context context) {
            super(context);
        }

        @Override
        protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent)
        {
            //This is where the magic happens, we have replaced the incoming maxOverScrollY with our own custom variable mMaxYOverscrollDistance;
            return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
        }
    }
}
