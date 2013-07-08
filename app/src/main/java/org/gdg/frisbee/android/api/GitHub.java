package org.gdg.frisbee.android.api;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.reflect.TypeToken;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.api.model.Directory;
import org.gdg.frisbee.android.api.model.Event;
import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 08.07.13
 * Time: 02:08
 * To change this template use File | Settings | File Templates.
 */
public class GitHub {

    private static final String BASE_URL = " https://api.github.com";
    private static final String CONTRIBUTORS_URL = BASE_URL + "/repos/%s/%s/contributors";

    public ApiRequest getContributors(String user, String repo, Response.Listener<ArrayList<Contributor>> successListener, Response.ErrorListener errorListener) {

        Type type = new TypeToken<ArrayList<Contributor>>() {}.getType();

        String url = String.format(CONTRIBUTORS_URL, user, repo);

        GsonRequest<ArrayList<Contributor>> eventReq = new GsonRequest<ArrayList<Contributor>>(Request.Method.GET,
                url,
                type,
                successListener,
                errorListener,
                GsonRequest.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));

        return new ApiRequest(eventReq);
    }
}
