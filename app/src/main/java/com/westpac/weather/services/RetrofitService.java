package com.westpac.weather.services;


import com.westpac.weather.models.GeoLocationData;
import com.westpac.weather.models.LatLng;
import com.westpac.weather.models.WeatherData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by yungang wu on 16/8/15.
 *
 */

/**
 * Provides the interface for {@link retrofit2.Retrofit} describing the endpoints
 * and responses for the endpoints.
 */

public interface RetrofitService {

    @GET("/forecast/{API_KEY}/{LATITUDE},{LONGITUDE}")
    Call<WeatherData> getWeatherData(@Path("API_KEY") String api_key,
                                     @Path("LATITUDE") double latitude,
                                     @Path("LONGITUDE") double longitude);

    @GET("/maps/api/geocode/json")
    Observable<GeoLocationData> getGeoLocationData(@Query("latlng") LatLng latLng,
                                                   @Query("result_type") String result_type,
                                                   @Query("key") String api_key);
}
