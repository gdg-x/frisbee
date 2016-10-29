package org.gdg.frisbee.android.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class PlusApiFactory {
    private static final String API_URL = "https://www.googleapis.com/plus/v1/";

    private PlusApiFactory() {
    }

    private static Retrofit provideRestAdapter(OkHttpClient okHttpClient) {
        OkHttpClient.Builder client = okHttpClient.newBuilder();
        client.addInterceptor(new ApiKeyAdder());

        return new Retrofit.Builder()
            .baseUrl(API_URL)
            .client(client.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }

    public static PlusApi providePlusApi(OkHttpClient okHttpClient) {
        return provideRestAdapter(okHttpClient).create(PlusApi.class);
    }
}
