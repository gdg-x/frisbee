package org.gdg.frisbee.android.api;

import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.reflect.TypeToken;
import org.gdg.frisbee.android.api.model.Gde;

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

    public ApiRequest getDirectory(final Response.Listener<HashMap<String, ArrayList<Gde>>> successListener, Response.ErrorListener errorListener) {
        Type type = new TypeToken<ArrayList<Gde>>() {}.getType();
        GsonRequest<Void, ArrayList<Gde>> dirReq = new GsonRequest<Void, ArrayList<Gde>>(Request.Method.GET,
                DIRECTORY_URL,
                type,
                new Response.Listener<ArrayList<Gde>>() {
                    @Override
                    public void onResponse(ArrayList<Gde> gdes) {
                        HashMap<String, ArrayList<Gde>> gdeMap = new HashMap<>();

                        for(Gde gde : gdes) {
                            if(!gdeMap.containsKey(gde.getProduct())) {
                                gdeMap.put(gde.getProduct(), new ArrayList<Gde>());
                            }

                            gdeMap.get(gde.getProduct()).add(gde);
                        }

                        if(successListener != null) {
                            successListener.onResponse(gdeMap);
                        }
                    }
                },
                errorListener,
                GsonRequest.getGson(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES));
        return new ApiRequest(dirReq);
    }
}
