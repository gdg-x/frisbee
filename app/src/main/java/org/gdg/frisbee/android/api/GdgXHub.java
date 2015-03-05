package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.api.model.TaggedEvent;
import org.joda.time.DateTime;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface GdgXHub {

    @GET("/chapters?perpage=-1")
    void getDirectory(Callback<Directory> callback);

    @GET("/events/{id}")
    void getEventDetail(@Path("id") String eventId, Callback<EventFullDetails> callback);

    @GET("/events/tag/{tag}/upcoming?perPage=1000")
    void getTaggedEventUpcomingList(@Path("tag") String tag, @Query("_") DateTime now, Callback<PagedList<TaggedEvent>> callback);

}
