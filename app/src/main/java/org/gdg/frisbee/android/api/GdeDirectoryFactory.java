package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.utils.Utils;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class GdeDirectoryFactory {

    private static final String API_URL = "https://gde-map.appspot.com";

    private GdeDirectoryFactory() {
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

    public static GdeDirectory provideGdeApi(OkHttpClient okHttpClient) {
        return provideRestAdapter(okHttpClient).create(GdeDirectory.class);
    }
}
