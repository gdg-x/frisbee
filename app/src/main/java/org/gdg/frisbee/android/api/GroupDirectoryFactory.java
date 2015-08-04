package org.gdg.frisbee.android.api;

import android.support.annotation.NonNull;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.utils.Utils;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by <a href="mailto:marcusandreog@gmail.com">Marcus Gabilheri</a>
 *
 * @author Marcus Gabilheri
 * @version 1.0
 * @since 7/26/15.
 */
public final class GroupDirectoryFactory {

    // This variable is meant to be changed as pleased while debugging
    private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.NONE;
    private static final String BASE_URL = "https://developers.google.com";

    private GroupDirectoryFactory() {
    }

    private static RestAdapter provideRestAdapter() {
        return new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setConverter(new GsonConverter(Utils.getGson()))
                    .setLogLevel(BuildConfig.DEBUG ? LOG_LEVEL : RestAdapter.LogLevel.NONE)
                    .setClient(OkClientFactory.provideClient())
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(@NonNull RequestFacade request) {
                            request.addHeader("User-Agent", "GDG-Frisbee/0.1 (Android)");
                            request.addHeader("Referer", "https://developers.google.com/groups/directory/");
                            request.addHeader("X-Requested-With", "XMLHttpRequest");
                            request.addHeader("Cache-Control", "no-cache");
                            request.addHeader("DNT", "1");
                        }
                    })
                    .build();
    }

    public static GroupDirectory provideGroupDirectoryApi() {
        return provideRestAdapter().create(GroupDirectory.class);
    }
}
