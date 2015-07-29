package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.BuildConfig;
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
public class GithubFactory {

    // This variable is meant to be changed as pleased while debugging
    private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.NONE;
    private static final String API_URL = "https://api.github.com";

    private GithubFactory() {
    }

    private static RestAdapter provideRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(API_URL)
                .setLogLevel(BuildConfig.DEBUG ? LOG_LEVEL : RestAdapter.LogLevel.NONE)
                .setConverter(new GsonConverter(Utils.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)))
                .setClient(OkClientFactory.provideClient())
                .build();
    }

    // this client is not used enough to justify making it a singleton
    public static GitHub provideGitHubApi() {
        return provideRestAdapter().create(GitHub.class);
    }
}
