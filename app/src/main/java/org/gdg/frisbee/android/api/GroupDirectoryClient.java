package org.gdg.frisbee.android.api;

import android.content.Context;

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
public final class GroupDirectoryClient {

    // This variable is meant to be changed as pleased while debugging
    private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.NONE;

    // The rest adapter that our api uses
    private static RestAdapter restAdapter;

    private static GroupDirectory groupDirectoryApi;

    private GroupDirectoryClient() {
    }

    /**
     * @param context
     *         The necessary context to instantiate this adapter
     * @return The rest adapter instance
     */
    public static RestAdapter getRestAdapter(Context context) {
        if (restAdapter == null) {
            restAdapter = new RestAdapter.Builder()
                    .setEndpoint(GroupDirectory.BASE_URL)
                    .setConverter(new GsonConverter(Utils.getGson()))
                    .setLogLevel(BuildConfig.DEBUG ? LOG_LEVEL : RestAdapter.LogLevel.NONE)
                    .setClient(OkClientProvider.getClient(context))
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addHeader("User-Agent", "GDG-Frisbee/0.1 (Android)");
                            request.addHeader("Referer", "https://developers.google.com/groups/directory/");
                            request.addHeader("X-Requested-With", "XMLHttpRequest");
                            request.addHeader("Cache-Control", "no-cache");
                            request.addHeader("DNT", "1");
                        }
                    })
                    .build();
        }
        return restAdapter;
    }

    public static GroupDirectory getGroupDirectoryApi(Context context) {
        if (groupDirectoryApi == null) {
            groupDirectoryApi = getRestAdapter(context).create(GroupDirectory.class);
        }
        return groupDirectoryApi;
    }
}
