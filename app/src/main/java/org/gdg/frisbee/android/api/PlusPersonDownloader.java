package org.gdg.frisbee.android.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.gdg.frisbee.android.Const;
import org.gdg.frisbee.android.app.App;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Intercepts the network request made by Picasso.
 * If it is a Google+ profile link, it makes a Plus API request first.
 * And then get the image and returns that image as the request link.
 *
 * Created by Said Tahsin Dane on 27/7/15.
 */
public class PlusPersonDownloader implements Interceptor {

    private static final Pattern mPlusPattern
            = Pattern.compile("http[s]?:\\/\\/plus\\..*google\\.com.*(\\+[a-zA-Z] +|[0-9]{21}).*");
    private Plus plusClient;

    public PlusPersonDownloader(Plus plusClient) {
        this.plusClient = plusClient;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        Matcher matcher = mPlusPattern.matcher(request.urlString());
        if (!matcher.matches()) {
            return chain.proceed(request);
        }

        String gplusId = matcher.group(1);
        Person person = getPersonSync(gplusId);
        if (person != null && person.getImage() != null && person.getImage().getUrl() != null) {
            String imageUrl = person.getImage().getUrl().replace("sz=50", "sz=196");
            return chain.proceed(request.newBuilder().url(imageUrl).build());
        }

        return chain.proceed(request);
    }

    @Nullable
    public Person getPersonSync(@NonNull final String gplusId) {

        final String cacheUrl = Const.CACHE_KEY_PERSON + gplusId;
        Object cachedPerson = App.getInstance().getModelCache().get(cacheUrl);
        Person person = null;

        if (cachedPerson instanceof Person) {
            person = (Person) cachedPerson;
            if (person.getImage() != null) {
                return person;
            }
        }
        if (cachedPerson != null) {
            App.getInstance().getModelCache().remove(cacheUrl);
        }

        try {
            Plus.People.Get request = plusClient.people().get(gplusId);
            request.setFields("aboutMe,cover/coverPhoto/url,image/url,displayName,tagline,urls");
            person = request.execute();
            App.getInstance().getModelCache().put(cacheUrl, person, DateTime.now().plusDays(2));
        } catch (IOException e) {
            Timber.e(e, "Error while getting profile URL.");
        }

        return person;
    }
}
