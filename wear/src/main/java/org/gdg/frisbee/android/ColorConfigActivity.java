package org.gdg.frisbee.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ColorConfigActivity extends Activity {
    public static final String CONFIG_HEADER = "org.gdgph.watchface.CONFIG_HEADER";
    public static final String CONFIG_COLOR = "org.gdgph.watchface.CONFIG_COLOR";

    private String mHeader;

    private WearableListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable_configuration);

        final TextView headerText = (TextView) findViewById(R.id.settings_header);
        if (getIntent().getExtras().containsKey(CONFIG_HEADER)) {
            mHeader = getIntent().getStringExtra(CONFIG_HEADER);
            headerText.setText(mHeader);
        } else {
            finish();
        }

        mListView = (WearableListView) findViewById(R.id.settings_list);
        mListView.addOnScrollListener(new WearableListView.OnScrollListener() {
            @Override
            public void onScroll(int i) {

            }

            @Override
            public void onAbsoluteScrollChange(int scroll) {
                float translation = Math.min(-scroll, 0);
                headerText.setTranslationY(translation);
            }

            @Override
            public void onScrollStateChanged(int i) {

            }

            @Override
            public void onCentralPositionChanged(int i) {

            }
        });

        displayColorSelections();
    }

    private void displayColorSelections() {
        List<String> colorList = new ArrayList<>();

        String[] colors = getResources().getStringArray(R.array.color_selection);
        for (String color : colors) {
            colorList.add(color);
        }

        ColorConfigAdapter adapter = new ColorConfigAdapter(this, colorList);
        mListView.setAdapter(adapter);
        mListView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                WearableListItemLayout layout = (WearableListItemLayout) viewHolder.itemView;
                CircledImageView circleImage = (CircledImageView) layout.findViewById(R.id.setting_circle);

                Intent intent = new Intent();
                intent.putExtra(CONFIG_HEADER, mHeader);
                intent.putExtra(CONFIG_COLOR, circleImage.getDefaultCircleColor());
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onTopEmptyRegionClick() {

            }
        });
    }
}
