package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.api.model.ImageInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PlusApi {
    @GET("people/{gplusId}?fields=image&key=" + BuildConfig.IP_SIMPLE_API_ACCESS_KEY)
    Call<ImageInfo> getImageInfo(@Path("gplusId") String gplusId);
}
