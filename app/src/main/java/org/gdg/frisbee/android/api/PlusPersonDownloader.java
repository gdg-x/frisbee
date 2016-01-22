package org.gdg.frisbee.android.api;

import android.support.annotation.Nullable;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class PlusPersonDownloader implements Interceptor {

    private static final Pattern mPlusPattern
            = Pattern.compile("http[s]?:\\/\\/plus\\..*google\\.com.*(\\+[a-zA-Z] +|[0-9]{21}).*");
    private Plus plusClient;

    public PlusPersonDownloader(Plus plusClient) {
        this.plusClient = plusClient;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        Matcher matcher = mPlusPattern.matcher(request.url().toString());
        if (!matcher.matches()) {
            return chain.proceed(request);
        }

        String gplusId = matcher.group(1);
        Person person = getPersonSync(plusClient, gplusId);
        if (person != null && person.getImage() != null && person.getImage().getUrl() != null) {
            String imageUrl = person.getImage().getUrl().replace("sz=50", "sz=196");
            return chain.proceed(request.newBuilder().url(imageUrl).build());
        }

        return null;
    }

    @Nullable
    public static Person getPersonSync(final String gplusId) {
        return getPersonSync(App.getInstance().getPlusClient(), gplusId);
    }

    @Nullable
    public static Person getPersonSync(final Plus plusClient, final String gplusId) {

        final String cacheUrl = Const.CACHE_KEY_PERSON + gplusId;
        Object cachedPerson = App.getInstance().getModelCache().get(cacheUrl);
        Person person = null;

        if (cachedPerson instanceof Person) {
            person = (Person) cachedPerson;
            if (person.getImage() != null) {
                Timber.d("Cache hit: %s", gplusId);
                return person;
            }
        }
        if (cachedPerson != null) {
            App.getInstance().getModelCache().remove(cacheUrl);
            Timber.d("Cache removal: %s", gplusId);
        }

        try {
            Plus.People.Get request = plusClient.people().get(gplusId);
            request.setFields("id,aboutMe,cover/coverPhoto/url,image/url,displayName,tagline,url,urls");
            person = request.execute();
            App.getInstance().getModelCache().put(cacheUrl, person, DateTime.now().plusDays(2));

            Timber.d("Request: %s", gplusId);
        } catch (IOException e) {
            Timber.e(e, "Error while getting profile URL.");
        }

        return person;
    }
}
