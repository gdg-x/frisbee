package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.api.model.HomeGdgRequest;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.api.model.PagedList;
import org.joda.time.DateTime;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GdgXHub {

    @GET("chapters?perpage=-1")
    Call<Directory> getDirectory();

    @GET("chapters/{chapterId}/events/{start}/{end}")
    Call<PagedList<Event>> getChapterEventList(@Path("chapterId") final String chapterId,
                                               @Path("start") final DateTime start,
                                               @Path("end") final DateTime end);

    @GET("events/{id}")
    Call<EventFullDetails> getEventDetail(@Path("id") String eventId);

    @GET("events/tag/{tag}/upcoming?perpage=-1")
    Call<PagedList<Event>> getTaggedEventUpcomingList(@Path("tag") String tag,
                                                      @Query("_") DateTime now);

    @PUT("frisbee/user/home")
    Call<Void> setHomeGdg(@Header("Authorization") String authorization,
                          @Body HomeGdgRequest request);

    @GET("organizer/{gplusId}")
    Call<OrganizerCheckResponse> checkOrganizer(@Path("gplusId") String gplusId);

}
