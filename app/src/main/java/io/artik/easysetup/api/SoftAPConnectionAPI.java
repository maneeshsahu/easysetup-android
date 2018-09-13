package io.artik.easysetup.api;

import com.google.gson.JsonObject;

import io.artik.easysetup.api.model.GetAPListBaseResponse;
import io.artik.easysetup.api.model.SoftAPGenericResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by vsingh on 06/03/17.
 */

public interface SoftAPConnectionAPI {
    @GET("wifi/accesspoints")
    Call<GetAPListBaseResponse> getAPList();

    @POST("wifi/config")
    Call<SoftAPGenericResponse> configureWifi(@Body JsonObject config);

//    @GET("artikcloud")
//    Call<SoftAPGetDTIDResponse> getDTID();
//
//    @GET("artikcloud/registration")
//    Call<SoftAPStartRegistrationResponse> startRegistration();
//
//    @PUT("artikcloud/registration")
//    Call<SoftAPGenericResponse> completeRegistration();
//
//    @POST("artikcloud")
//    Call<SoftAPGenericResponse> provisionDevice(@Body JsonObject provisionConfig);
}
