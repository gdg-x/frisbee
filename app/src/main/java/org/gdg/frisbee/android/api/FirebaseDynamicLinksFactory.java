package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.utils.Utils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class FirebaseDynamicLinksFactory {
    private static final String BASE_URL = "https://firebasedynamiclinks.googleapis.com";

    private FirebaseDynamicLinksFactory() {
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

    public static FirebaseDynamicLinks provideFirebaseDynamicLinksApi(OkHttpClient okHttpClient) {
        return provideRestAdapter(okHttpClient).create(FirebaseDynamicLinks.class);
    }
}
