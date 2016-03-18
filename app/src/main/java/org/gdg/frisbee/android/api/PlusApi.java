package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.plus.Activities;
import org.gdg.frisbee.android.api.model.plus.ImageInfo;
import org.gdg.frisbee.android.api.model.plus.Person;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PlusApi {
    @GET("people/{gplusId}?fields=image")
    Call<ImageInfo> getImageInfo(@Path("gplusId") String gplusId);

    @GET("people/{gplusId}?fields=image,aboutMe,tagline,urls,url,cover,displayName")
    Call<Person> getPerson(@Path("gplusId") String gplusId);

    @GET("people/{gplusId}/activities/public?fields="
        + "nextPageToken,"
        + "items(id,published,url,object/content,verb,"
        + "object/attachments,annotation,object(plusoners,replies,resharers))")
    Call<Activities> getActivities(@Path("gplusId") String gplusId);
}
