package org.gdg.frisbee.android.api;

import org.gdg.frisbee.android.api.model.GdeList;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GdeDirectory {
    
    @GET("gde/list")
    Call<GdeList> getDirectory();
}
