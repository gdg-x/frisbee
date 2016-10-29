package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.utils.Utils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class GithubFactory {

    private static final String API_URL = "https://api.github.com";

    private GithubFactory() {
    }

    private static Retrofit provideRestAdapter(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl(API_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(Utils.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES))
            )
            .build();
    }

    public static GitHub provideGitHubApi(OkHttpClient okHttpClient) {
        return provideRestAdapter(okHttpClient).create(GitHub.class);
    }
}
