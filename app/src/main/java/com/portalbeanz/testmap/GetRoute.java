package com.portalbeanz.testmap;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetRoute {
    @GET("json")
    Call<JsonObject> getPolylineData(@Query("origin") String origin, @Query("destination") String destination);
}
