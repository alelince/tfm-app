package com.tfm.bleapp.rest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IServiceAPI {

    @GET("/")
    Call<Void> heartbeat();

    @GET("/scenarios")
    Call<List<String>> listScenarios();

    @GET("/blueprints/{scenario}")
    Call<ResponseBody> getBlueprint(@Path("scenario") String scenario);

    @GET("/campaigns")
    Call<List<Campaign>> listCampaigns();

    @GET("/campaigns/{campaign_name}/points")
    Call<List<AcquisitionPoint>> listPoints(@Path("campaign_name") String campaignName);

    @Headers("Content-type: application/json")
    @POST("/campaigns/{campaign_name}/points/{point_id}/samples")
    Call<Void> createSamples(@Path("campaign_name") String campaignName,
                             @Path("point_id") int pointId,
                             @Body List<AcquisitionSample> samples);

    @Headers("Content-type: application/json")
    @POST("/rtls/{scenario}")
    Call<LocationResult> locate(@Path("scenario") String scenario, @Body LocationRequest request);

}
