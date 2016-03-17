package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.BuildConfig;
import org.gdg.frisbee.android.api.model.plus.ImageInfo;
import org.gdg.frisbee.android.api.model.plus.Person;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PlusApi {
    @GET("people/{gplusId}?fields=image&key=" + BuildConfig.IP_SIMPLE_API_ACCESS_KEY)
    Call<ImageInfo> getImageInfo(@Path("gplusId") String gplusId);

    @GET("people/{gplusId}?fields=image,aboutMe,tagline,urls,url,cover,displayName&key="
        + BuildConfig.IP_SIMPLE_API_ACCESS_KEY)
    Person getPerson(@Path("gplusId") String gplusId);
}
