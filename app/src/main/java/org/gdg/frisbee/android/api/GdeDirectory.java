package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.Gde;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.http.GET;

public interface GdeDirectory {
    
    @GET("/gde/list")
    void getDirectory(Callback<ArrayList<Gde>> callback);
}
