package org.gdg.frisbee.android.api;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.Utils;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

public final class GroupDirectoryFactory {

    private static final String BASE_URL = "https://developers.google.com";

    private GroupDirectoryFactory() {
    }

    private static Retrofit provideRestAdapter() {
        OkHttpClient client = App.getInstance().getOkHttpClient().clone();
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {

                Request compressedRequest = chain.request().newBuilder()
                        .header("User-Agent", "GDG-Frisbee/0.1 (Android)")
                        .header("Referer", "https://developers.google.com/groups/directory/")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .header("Cache-Control", "no-cache")
                        .header("DNT", "1")
                        .build();
                return chain.proceed(compressedRequest);

            }
        });
        return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(Utils.getGson()))
                    .client(client)
                    .build();
    }

    public static GroupDirectory provideGroupDirectoryApi() {
        return provideRestAdapter().create(GroupDirectory.class);
    }
}
