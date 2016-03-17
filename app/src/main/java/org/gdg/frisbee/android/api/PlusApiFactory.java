package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.app.App;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlusApiFactory {
    private static final String API_URL = "https://www.googleapis.com/plus/v1/";

    private PlusApiFactory() {
    }

    private static Retrofit provideRestAdapter() {
        return new Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(SynchronousCallAdapterFactory.create())
            .client(OkClientFactory.okHttpClientWithIdlingResources(App.getInstance().getOkHttpClient()))
            .build();
    }

    public static PlusApi providePlusApi() {
        return provideRestAdapter().create(PlusApi.class);
    }
}
