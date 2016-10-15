package org.gdg.frisbee.android.chapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import org.gdg.frisbee.android.api.model.plus.Person;
import org.gdg.frisbee.android.app.App;
import org.gdg.frisbee.android.cache.ModelCache;
import org.gdg.frisbee.android.utils.Utils;
import org.joda.time.DateTime;

import java.io.IOException;

import retrofit2.Response;

class OrganizerLoader extends AsyncTask<String, Person, Void> {
    private final boolean online;
    private Listener listener;

    OrganizerLoader(Context context) {
        online = Utils.isOnline(context);
    }

    @Override
    protected Void doInBackground(String... organizerIds) {
        for (String gplusId : organizerIds) {
            publishProgress(loadOrganizer(gplusId));
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Person... values) {
        if (listener == null) {
            return;
        }
        Person organizer = values[0];
        if (organizer != null) {
            listener.onOrganizerLoaded(organizer);
        } else {
            listener.onUnknownOrganizerLoaded();
        }
    }

    @Nullable
    private Person loadOrganizer(String gplusId) {
        Person person = App.getInstance().getModelCache()
            .get(ModelCache.KEY_PERSON + gplusId, online);
        if (person != null) {
            return person;
        }
        try {
            Response<Person> response = App.getInstance().getPlusApi().
                getPerson(gplusId).execute();
            if (response.isSuccessful()) {
                person = response.body();
                putPersonInCache(gplusId, person);
                return person;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private static void putPersonInCache(String plusId, Person person) {
        App.getInstance().getModelCache().putAsync(
            ModelCache.KEY_PERSON + plusId,
            person,
            DateTime.now().plusDays(1),
            null
        );
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onOrganizerLoaded(Person organizer);

        void onUnknownOrganizerLoaded();
    }
}
