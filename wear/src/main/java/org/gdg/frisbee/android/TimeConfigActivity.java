package org.gdg.frisbee.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TimeConfigActivity extends Activity {
    public static final String CONFIG_HEADER = "org.gdgph.watchface.time.header";
    public static final String CONFIG_VALUE = "org.gdgph.watchface.time.value";

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

        displayTimeSelection();
    }

    private void displayTimeSelection() {
        List<String> timeSettings = new ArrayList<>();

        String[] timeSetting = getResources().getStringArray(R.array.time_selection);
        for (String setting : timeSetting) {
            timeSettings.add(setting);
        }

        TimeConfigAdapter adapter = new TimeConfigAdapter(this, timeSettings);
        mListView.setAdapter(adapter);
        mListView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                WearableListItemLayout layout = (WearableListItemLayout) viewHolder.itemView;
                TextView nameTextView = (TextView) layout.findViewById(R.id.setting_text_view);

                Intent intent = new Intent();
                intent.putExtra(CONFIG_HEADER, mHeader);
                intent.putExtra(CONFIG_VALUE, getTimeValue(nameTextView.getText().toString()));
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onTopEmptyRegionClick() {

            }
        });
    }

    private int getTimeValue(String timeSetting) {
        if(getString(R.string.time_12).equals(timeSetting)) {
            return WearableConfigurationUtil.TIME_12_HOUR;
        } else if(getString(R.string.time_24).equals(timeSetting)) {
            return WearableConfigurationUtil.TIME_24_HOUR;
        } else {
            return 0;
        }
    }
}
