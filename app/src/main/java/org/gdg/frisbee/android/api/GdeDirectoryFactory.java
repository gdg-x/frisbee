package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;

import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

public class GdeDirectoryFactory {

    private static final String API_URL = "https://gde-map.appspot.com";

    private GdeDirectoryFactory() {
    }

    private static Retrofit provideRestAdapter() {
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .client(OkClientFactory.okHttpClientWithIdlingResources(App.getInstance().getOkHttpClient()))
                .addConverterFactory(GsonConverterFactory.create(Utils.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)))
                .build();
    }

    public static GdeDirectory provideGdeApi() {
        return provideRestAdapter().create(GdeDirectory.class);
    }
}
