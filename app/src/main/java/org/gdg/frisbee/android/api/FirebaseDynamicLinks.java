package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.FirebaseDynamicLinksRequest;
import org.gdg.frisbee.android.api.model.FirebaseDynamicLinksResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FirebaseDynamicLinks {

    @POST("/v1/shortLinks")
    Call<FirebaseDynamicLinksResponse> shortenUrl(@Query("key") String apiKey, @Body FirebaseDynamicLinksRequest firebaseDynamicLinksRequest);
}
