package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class GithubFactory {

    // This variable is meant to be changed as pleased while debugging
//    private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.NONE;
    private static final String API_URL = "https://api.github.com";

    private GithubFactory() {
    }

    private static Retrofit provideRestAdapter() {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(Utils.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)))
                .client(App.getInstance().getOkHttpClient())
                .build();
    }

    // this client is not used enough to justify making it a singleton
    public static GitHub provideGitHubApi() {
        return provideRestAdapter().create(GitHub.class);
    }
}
