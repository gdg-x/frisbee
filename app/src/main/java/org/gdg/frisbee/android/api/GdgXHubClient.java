package org.gdg.frisbee.android.api;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.api.deserializer.ZuluDateTimeDeserializer;
import org.gdg.frisbee.android.utils.Utils;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by <a href="mailto:marcusandreog@gmail.com">Marcus Gabilheri</a>
 *
 * @author Marcus Gabilheri
 * @version 1.0
 * @since 7/26/15.
 */
public final class GdgXHubClient {

    // This variable is meant to be changed as pleased while debugging
    private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.NONE;

    // The rest adapter that our api uses
    private static RestAdapter restAdapter;

    private static GdgXHub hubApi;

    private GdgXHubClient() {
    }

    /**
     * @param context
     *         The necessary context to instantiate this adapter
     * @return The rest adapter instance
     */
    public static RestAdapter getRestAdapter(Context context) {
        if (restAdapter == null) {
            restAdapter = new RestAdapter.Builder()
                    .setEndpoint(GdgXHub.BASE_URL)
                    .setLogLevel(BuildConfig.DEBUG ? LOG_LEVEL : RestAdapter.LogLevel.NONE)
                    .setConverter(new GsonConverter(Utils.getGson(FieldNamingPolicy.IDENTITY, new ZuluDateTimeDeserializer())))
                    .setClient(OkClientProvider.getClient(context))
                    .build();
        }
        return restAdapter;
    }

    public static GdgXHub getHubApi(Context context) {
        if (hubApi == null) {
            hubApi = getRestAdapter(context).create(GdgXHub.class);
        }
        return hubApi;
    }
}
