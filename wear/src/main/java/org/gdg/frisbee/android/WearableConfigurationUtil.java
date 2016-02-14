package org.gdg.frisbee.android;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class WearableConfigurationUtil {

    public static final String PATH_ANALOG = "/watch_face_config/Analog";

    public static final String CONFIG_BACKGROUND = "Background";
    public static final String CONFIG_DATE = "Date";
    public static final String CONFIG_DATE_TIME = "Date/Time Color";
    public static final String CONFIG_DIGITAL_TIME = "Digital Time";
    public static final String CONFIG_HAND_HOUR = "Hour Hand";
    public static final String CONFIG_HAND_MINUTE = "Minute Hand";
    public static final String CONFIG_HAND_SECOND = "Second Hand";
    public static final String CONFIG_HOUR_MARKER = "Hour Marker";

    public static final int TIME_12_HOUR = 12;
    public static final int TIME_24_HOUR = 24;

    public static void updateKeysInConfigDataMap(final GoogleApiClient googleApiClient, final String path,
                                                 final DataMap configKeysToUpdate) {
        fetchConfigDataMap(googleApiClient, path,
            new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    if (dataItemResult.getStatus().isSuccess()) {
                        DataMap config = new DataMap();
                        if (dataItemResult.getDataItem() != null) {
                            DataItem configDataItem = dataItemResult.getDataItem();
                            DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                            config = dataMapItem.getDataMap();
                        }

                        DataMap updatedConfig = new DataMap();
                        updatedConfig.putAll(config);
                        updatedConfig.putAll(configKeysToUpdate);
                        putConfigDataItem(googleApiClient, path, updatedConfig);
                    }
                }
            });
    }

    public static void fetchConfigDataMap(final GoogleApiClient client, final String path,
                                          final ResultCallback<DataApi.DataItemResult> callback) {
        Wearable.NodeApi.getLocalNode(client).setResultCallback(
            new ResultCallback<NodeApi.GetLocalNodeResult>() {
                @Override
                public void onResult(NodeApi.GetLocalNodeResult getLocalNodeResult) {
                    String localNode = getLocalNodeResult.getNode().getId();
                    Uri uri = new Uri.Builder()
                        .scheme("wear")
                        .path(path)
                        .authority(localNode)
                        .build();
                    Wearable.DataApi.getDataItem(client, uri)
                        .setResultCallback(callback);
                }
            }
        );
    }

    private static void putConfigDataItem(GoogleApiClient googleApiClient, final String path, DataMap updatedConfig) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(path);
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(updatedConfig);
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
            .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.d("WearableConfig", "putDataItem result status: " + dataItemResult.getStatus());
                }
            });
    }

}
