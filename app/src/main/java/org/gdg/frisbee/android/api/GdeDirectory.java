package org.gdg.frisbee.android.api;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.reflect.TypeToken;
import org.gdg.frisbee.android.api.model.Gde;
import org.gdg.frisbee.android.api.model.GdeList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by maui on 28.05.2014.
 */
public class GdeDirectory {

    private static final String DIRECTORY_URL = "https://gde-map.appspot.com/gde/list";

    private static final String LOG_TAG = "GDG-GdeDirectory";

    public GdeDirectory() {
    }

    public ApiRequest getDirectory(final Response.Listener<GdeList> successListener, Response.ErrorListener errorListener) {
        GsonRequest<Void, GdeList> dirReq = new GsonRequest<Void, GdeList>(Request.Method.GET,
                DIRECTORY_URL,
                GdeList.class,
                successListener,
                errorListener,
                GsonRequest.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));
        return new ApiRequest(dirReq);
    }
}
