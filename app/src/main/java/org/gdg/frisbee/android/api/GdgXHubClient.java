package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.api.deserializer.ZuluDateTimeDeserializer;
import org.gdg.frisbee.android.app.App;
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

    private GdgXHubClient() {
    }

    private static RestAdapter getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(GdgXHub.BASE_URL)
                .setLogLevel(BuildConfig.DEBUG ? LOG_LEVEL : RestAdapter.LogLevel.NONE)
                .setConverter(new GsonConverter(Utils.getGson(FieldNamingPolicy.IDENTITY, new ZuluDateTimeDeserializer())))
                .setClient(App.getInstance().getRetrofitClient())
                .build();
    }

    public static GdgXHub getHubApi() {
        return getRestAdapter().create(GdgXHub.class);
    }
}
