package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public class GithubFactory {

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

    public static GitHub provideGitHubApi() {
        return provideRestAdapter().create(GitHub.class);
    }
}
