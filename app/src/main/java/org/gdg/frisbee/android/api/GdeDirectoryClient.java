package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by <a href="mailto:marcusandreog@gmail.com">Marcus Gabilheri</a>
 *
 * @author Marcus Gabilheri
 * @version 1.0
 * @since 7/28/15.
 */
public class GdeDirectoryClient {

    // This variable is meant to be changed as pleased while debugging
    private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.NONE;
    public static final String API_URL = "https://gde-map.appspot.com";

    private GdeDirectoryClient() {
    }

    // We can make this a singleton to avoid instantiating this twice
    private static GdeDirectory gdeDirectory;

    private static RestAdapter getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setClient(App.getInstance().getRetrofitClient())
                .setLogLevel(BuildConfig.DEBUG ? LOG_LEVEL : RestAdapter.LogLevel.NONE)
                .setConverter(new GsonConverter(Utils.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)))
                .build();
    }

    public static GdeDirectory getGdeApi() {
        if (gdeDirectory == null) {
            gdeDirectory = getRestAdapter().create(GdeDirectory.class);
        }
        return gdeDirectory;
    }
}
