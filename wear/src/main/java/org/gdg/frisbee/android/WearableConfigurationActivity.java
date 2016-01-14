package org.gdg.frisbee.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

public class WearableConfigurationActivity extends Activity implements DataApi.DataListener {
    public static final String PATH_BACKGROUND = "/watchface/background";
    public static final String PATH_DATE = "/watchface/date";
    public static final String PATH_HOUR_HAND = "/watchface/hour_hand";
    public static final String PATH_MINUTE_HAND = "/watchface/minute_hand";
    public static final String PATH_SECOND_HAND = "/watchface/second_hand";
    public static final String PATH_HOUR_MARKER = "/watchface/hour_marker";
    public static final String CONFIG_BACKGROUND = "Background";
    public static final String CONFIG_DATE = "Date";
    public static final String CONFIG_HAND_HOUR = "Hour Hand";
    public static final String CONFIG_HAND_MINUTE = "Minute Hand";
    public static final String CONFIG_HAND_SECOND = "Second Hand";
    public static final String CONFIG_HOUR_MARKER = "Hour Marker";

    private static final String TAG = "WearableConfigActivity";
    private static final int REQUEST_COLOR = 0;

    private WearableListView mListView;
    private WearableConfigAdapter mAdapter;
    private GoogleApiClient mGoogleApiClient;
    private boolean mDisplayDate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wearable_configuration);

        final TextView headerText = (TextView) findViewById(R.id.settings_header);

        mListView = (WearableListView) findViewById(R.id.settings_list);
        mAdapter = new WearableConfigAdapter(this, getConfigurations());
        mListView.setAdapter(mAdapter);
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
        mListView.setClickListener(new WearableListView.ClickListener() {
            @Override
            public void onClick(WearableListView.ViewHolder viewHolder) {
                WearableListItemLayout layout = (WearableListItemLayout) viewHolder.itemView;

                TextView nameTextView = (TextView) layout.findViewById(R.id.setting_text_view);
                String action = nameTextView.getText().toString();
                if (action.contains("Background") || action.contains("Hand") || action.contains("Marker")) {
                    Intent intent = new Intent(WearableConfigurationActivity.this, ColorConfigActivity.class);
                    intent.putExtra(ColorConfigActivity.CONFIG_HEADER, action);
                    startActivityForResult(intent, REQUEST_COLOR);
                } else if (CONFIG_DATE.equals(action)) {
                    TextView settingTextView = (TextView) layout.findViewById(R.id.subsetting_text_view);
                    CircledImageView circleImage = (CircledImageView) layout.findViewById(R.id.setting_circle);
                    PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_DATE);

                    if (getString(R.string.label_setting_on).equals(settingTextView.getText().toString())) {
                        settingTextView.setText(getString(R.string.label_setting_off));
                        circleImage.setImageResource(R.drawable.ic_date_off);
                        putDataMapRequest.getDataMap().putBoolean(action, false);
                        mDisplayDate = false;
                        updateConfigurations();
                    } else {
                        settingTextView.setText(getString(R.string.label_setting_on));
                        circleImage.setImageResource(R.drawable.ic_date_on);
                        putDataMapRequest.getDataMap().putBoolean(action, true);
                        mDisplayDate = true;
                        updateConfigurations();
                    }

                    PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                    Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
                }
            }

            @Override
            public void onTopEmptyRegionClick() {

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected:" + bundle);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended:" + i);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:");
                    }
                })
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.getDataItems(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DataItemBuffer>() {
                    @Override
                    public void onResult(DataItemBuffer dataItems) {
                        for (DataItem item : dataItems) {
                            updateConfig(item);
                        }

                        dataItems.release();
                    }
                });
    }

    private void updateConfig(DataItem item) {
        if (WearableConfigurationActivity.PATH_DATE.equals(item.getUri().getPath())) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            if (dataMap.containsKey(WearableConfigurationActivity.CONFIG_DATE)) {
                mDisplayDate = dataMap.getBoolean(WearableConfigurationActivity.CONFIG_DATE, true);
                updateConfigurations();
            }
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_COLOR && resultCode == RESULT_OK) {
            int color = data.getIntExtra(ColorConfigActivity.CONFIG_COLOR, 0);
            String action = data.getStringExtra(ColorConfigActivity.CONFIG_HEADER);

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(getDataPath(action));
            putDataMapRequest.getDataMap().putInt(action, color);
            PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        }
    }

    private String getDataPath(String action) {
        if (CONFIG_BACKGROUND.equals(action)) {
            return PATH_BACKGROUND;
        } else if (CONFIG_HAND_HOUR.equals(action)) {
            return PATH_HOUR_HAND;
        } else if (CONFIG_HAND_MINUTE.equals(action)) {
            return PATH_MINUTE_HAND;
        } else if (CONFIG_HAND_SECOND.equals(action)) {
            return PATH_SECOND_HAND;
        } else if (CONFIG_HOUR_MARKER.equals(action)) {
            return PATH_HOUR_MARKER;
        } else {
            return "";
        }
    }

    private void updateConfigurations() {
        mAdapter.setConfigurations(getConfigurations());
    }

    private List<WearableConfiguration> getConfigurations() {
        List<WearableConfiguration> configurationList = new ArrayList<>();
//        configurationList.add(new WearableConfiguration(R.drawable.ic_palette, CONFIG_BACKGROUND));
        configurationList.add(new WearableConfiguration(R.drawable.ic_date_on, CONFIG_DATE, mDisplayDate));
        configurationList.add(new WearableConfiguration(R.drawable.ic_palette, CONFIG_HAND_HOUR));
        configurationList.add(new WearableConfiguration(R.drawable.ic_palette, CONFIG_HAND_MINUTE));
        configurationList.add(new WearableConfiguration(R.drawable.ic_palette, CONFIG_HAND_SECOND));
        configurationList.add(new WearableConfiguration(R.drawable.ic_palette, CONFIG_HOUR_MARKER));
        return configurationList;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent dataEvent : dataEventBuffer) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = dataEvent.getDataItem();
                updateConfig(item);
            }
        }

        dataEventBuffer.release();
    }
}
