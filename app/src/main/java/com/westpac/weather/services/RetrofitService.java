package com.westpac.weather.services;


import com.westpac.weather.models.WeatherData;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by yungang wu on 16/8/15.
 *
 */

/**
 * Provides the interface for {@link retrofit.Retrofit} describing the endpoints and responses for the endpoints.
 */

public interface RetrofitService {

    @GET("/forecast/{APIKEY}/{LATITUDE},{LONGITUDE}")
    Call<WeatherData> getWeatherData(@Path("APIKEY") String apikey, @Path("LATITUDE") double latitude, @Path("LONGITUDE") double longitude);
}
