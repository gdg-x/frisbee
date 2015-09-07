package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.api.model.GcmRegistrationRequest;
import org.gdg.frisbee.android.api.model.GcmRegistrationResponse;
import org.gdg.frisbee.android.api.model.HomeGdgRequest;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.api.model.PagedList;
import org.gdg.frisbee.android.api.model.TaggedEvent;
import org.joda.time.DateTime;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface GdgXHub {

    @GET("chapters?perpage=-1")
    Call<Directory> getDirectory();

    @GET("events/{id}")
    Call<EventFullDetails> getEventDetail(@Path("id") String eventId);

    @GET("events/tag/{tag}/upcoming?perpage=-1")
    Call<PagedList<TaggedEvent>> getTaggedEventUpcomingList(@Path("tag") String tag,
                                    @Query("_") DateTime now);

    @POST("frisbee/gcm/register")
    Call<GcmRegistrationResponse> registerGcm(@Header("Authorization") String authorization,
                     @Body GcmRegistrationRequest request);

    @POST("frisbee/gcm/unregister")
    Call<GcmRegistrationResponse> unregisterGcm(@Header("Authorization") String authorization,
                       @Body GcmRegistrationRequest request);

    @PUT("frisbee/user/home")
    Call<Void> setHomeGdg(@Header("Authorization") String authorization,
                    @Body HomeGdgRequest request);

    @GET("organizer/{gplusId}")
    Call<OrganizerCheckResponse> checkOrganizer(@Path("gplusId") String gplusId);

}
