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

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

public interface GdgXHub {

    @GET("/chapters?perpage=-1")
    void getDirectory(Callback<Directory> callback);

    @GET("/events/{id}")
    void getEventDetail(@Path("id") String eventId,
                        Callback<EventFullDetails> callback);

    @GET("/events/tag/{tag}/upcoming?perpage=-1")
    void getTaggedEventUpcomingList(@Path("tag") String tag,
                                    @Query("_") DateTime now,
                                    Callback<PagedList<TaggedEvent>> callback);

    @POST("/frisbee/gcm/register")
    void registerGcm(@Header("Authorization") String authorization,
                     @Body GcmRegistrationRequest request,
                     Callback<GcmRegistrationResponse> callback);

    @POST("/frisbee/gcm/unregister")
    void unregisterGcm(@Header("Authorization") String authorization,
                       @Body GcmRegistrationRequest request,
                       Callback<GcmRegistrationResponse> callback);

    @PUT("/user/home")
    void setHomeGdg(@Header("Authorization") String authorization,
                    @Body HomeGdgRequest request,
                    Callback<Void> callback);

    @GET("/organizer/{gplusId}")
    void checkOrganizer(@Path("gplusId") String gplusId,
                        Callback<OrganizerCheckResponse> callback);

}
