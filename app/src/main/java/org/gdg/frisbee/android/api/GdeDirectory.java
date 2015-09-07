package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.GdeList;

import retrofit.Call;
import retrofit.http.GET;

public interface GdeDirectory {
    
    @GET("gde/list")
    Call<GdeList> getDirectory();
}
