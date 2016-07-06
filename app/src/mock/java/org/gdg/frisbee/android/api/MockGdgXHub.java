package org.gdg.frisbee.android.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.gdg.frisbee.android.api.deserializer.ZuluDateTimeDeserializer;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.gdg.frisbee.android.api.model.EventFullDetails;
import org.gdg.frisbee.android.api.model.HomeGdgRequest;
import org.gdg.frisbee.android.api.model.OrganizerCheckResponse;
import org.gdg.frisbee.android.api.model.PagedList;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.utils.MockUtils;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.mock.Calls;

public class MockGdgXHub implements GdgXHub {

    private Gson gson;

    public MockGdgXHub() {
        gson = Utils.getGson(FieldNamingPolicy.IDENTITY, new ZuluDateTimeDeserializer());
    }

    @Override
    public Call<Directory> getDirectory() {
        Directory directory = new Directory();
        try {
            directory = gson.fromJson(
                MockUtils.getStringFromFile(App.getInstance(), "chapters.json"),
                Directory.class
            );
        } catch (IOException ignored) {
        }
        return Calls.response(directory);
    }

    @Override
    public Call<PagedList<Event>> getChapterEventList(@Path("chapterId") String chapterId, @Path("start") DateTime start, @Path("end") DateTime end) {
        PagedList<Event> events = new PagedList<>();
        try {
            events = gson.fromJson(
                MockUtils.getStringFromFile(App.getInstance(), "chapters/105068877693379070381/events.json"),
                new TypeToken<PagedList<Event>>() {}.getType()
            );
        } catch (IOException ignored) {
        }
        return Calls.response(events);
    }

    @Override
    public Call<EventFullDetails> getEventDetail(@Path("id") String eventId) {
        EventFullDetails eventFullDetails = new EventFullDetails();
        try {
            eventFullDetails = gson.fromJson(
                MockUtils.getStringFromFile(App.getInstance(), "events/6256487006994432.json"),
                EventFullDetails.class
            );
        } catch (IOException ignored) {
        }
        return Calls.response(eventFullDetails);
    }

    @Override
    public Call<PagedList<Event>> getTaggedEventUpcomingList(@Path("tag") String tag, @Query("_") DateTime now) {
        return Calls.response(new PagedList<Event>());
    }

    @Override
    public Call<Void> setHomeGdg(@Header("Authorization") String authorization, @Body HomeGdgRequest request) {
        return Calls.failure(new IOException("Set Home Gdg is not working"));
    }

    @Override
    public Call<OrganizerCheckResponse> checkOrganizer(@Path("gplusId") String gplusId) {
        return Calls.response(new OrganizerCheckResponse());
    }
}
