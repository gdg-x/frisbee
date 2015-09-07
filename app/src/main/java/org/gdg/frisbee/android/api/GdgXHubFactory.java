package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;

import org.gdg.frisbee.android.api.deserializer.ZuluDateTimeDeserializer;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public final class GdgXHubFactory {

    private static final String BASE_URL = "https://hub.gdgx.io/api/v1/";

    private GdgXHubFactory() {
    }

    private static Retrofit provideRestAdapter() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(App.getInstance().getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create(Utils.getGson(FieldNamingPolicy.IDENTITY, new ZuluDateTimeDeserializer())))
                .build();
    }

    public static GdgXHub provideHubApi() {
        return provideRestAdapter().create(GdgXHub.class);
    }
}
