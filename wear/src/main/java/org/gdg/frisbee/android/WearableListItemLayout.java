package org.gdg.frisbee.android;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class WearableListItemLayout extends FrameLayout implements WearableListView.OnCenterProximityListener {

    private CircledImageView mCircle;
    private TextView mName;
    private TextView mOnOffText;

    public WearableListItemLayout(Context context) {
        super(context);
        View.inflate(context, R.layout.list_wearable_config, this);
        mCircle = (CircledImageView) findViewById(R.id.setting_circle);
        mName = (TextView) findViewById(R.id.setting_text_view);
        mOnOffText = (TextView) findViewById(R.id.subsetting_text_view);

    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCircle = (CircledImageView) findViewById(R.id.setting_circle);
        mName = (TextView) findViewById(R.id.setting_text_view);
        mOnOffText = (TextView) findViewById(R.id.subsetting_text_view);
    }

    @Override
    public void onCenterPosition(boolean b) {
        mCircle.animate().scaleX(1f).scaleY(1f).alpha(1);
        mName.animate().scaleX(1f).scaleY(1f).alpha(1);
        mOnOffText.animate().scaleX(1f).scaleY(1f).alpha(1);
//        ((GradientDrawable) mCircle.getDrawable()).setColor(mChosenCircleColor);
    }

    @Override
    public void onNonCenterPosition(boolean b) {
        mCircle.animate().scaleX(0.8f).scaleY(0.8f).alpha(06f);
        mName.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
        mOnOffText.animate().scaleX(0.8f).scaleY(0.8f).alpha(0.6f);
    }

}
