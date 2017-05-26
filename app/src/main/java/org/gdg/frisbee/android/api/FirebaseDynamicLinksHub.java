package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.FirebaseDynamicLinksResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by unstablebrainiac on 26/5/17.
 */

public interface FirebaseDynamicLinksHub {

    @FormUrlEncoded
    @POST("/v1/shortLinks")
    Call<FirebaseDynamicLinksResponse> getShortenedUrl(@Query("key") String api_key, @Field("longDynamicLink") String long_dynamic_url);
}
