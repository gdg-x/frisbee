package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.utils.Utils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by unstablebrainiac on 26/5/17.
 */

public final class FirebaseDynamicLinksHubFactory {
    private static final String BASE_URL = "https://firebasedynamiclinks.googleapis.com";

    private FirebaseDynamicLinksHubFactory() {
    }

    private static Retrofit provideRestAdapter(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(Utils.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES))
            )
            .build();
    }

    public static FirebaseDynamicLinksHub provideFirebaseDynamicLinksApi(OkHttpClient okHttpClient) {
        return provideRestAdapter(okHttpClient).create(FirebaseDynamicLinksHub.class);
    }
}
