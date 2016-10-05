package com.solariswu.weather.services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.solariswu.weather.models.GeoLocationData;
import com.solariswu.weather.models.LatLng;
import com.solariswu.weather.models.WeatherData;
import com.solariswu.weather.ui.WeatherView;

import javax.annotation.Nullable;

import retrofit2.Retrofit;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by solariswu on 16/10/5.
 *
 */

public class WeatherPresenter {

    //Log Identification String
    private static final String WEATHERPRESENTER_LOG = "weather_presenter";


    //Retrofit Service variables
    private RetrofitService mGeoLocationService;
    private RetrofitService mWeatherService;

    //DARK Sky weather API_KEY, url and data notification ID
    private static final String STR_WEATHER_API_KEY      = "677bb1722471bc47d236bc195384c273";
    private static final String STR_WEATHER_URL          = "https://api.darksky.net/";

    //Google GEOLOCATION API_KEY, url and data notification ID
    private static final String STR_GEOLOCATION_API_KEY  = "AIzaSyCsPFEM6kbaJSdnCugIfexgeo9w_7zSnbA";
    private static final String STR_GEOLOCATION_URL         = "https://maps.googleapis.com/";
    private static final String STR_GEOLOCATION_RESULT_TYPE = "country|locality";


    @Nullable
    private WeatherView mView;

    public void onCreate(@NonNull WeatherView weatherViewView) {
        mView = weatherViewView;

        // call View's implemented methods
        mView.initViews();

        // prepare the retrofit services
        Retrofit geoLocationRetrofit = RetrofitManager.getInstance()
                .buildGeoLocationRetrofit(STR_GEOLOCATION_URL);

        mGeoLocationService = geoLocationRetrofit.create(RetrofitService.class);

        Retrofit weatherRetrofit = RetrofitManager.getInstance()
                .buildWeatherRetrofit(STR_WEATHER_URL);

        mWeatherService = weatherRetrofit.create(RetrofitService.class);

    }

    public void fetchWeatherData (LatLng latLng) {
        Log.i(WEATHERPRESENTER_LOG, "Fetch weather Data LATLNG:"+ latLng);

        mWeatherService.getWeatherData(STR_WEATHER_API_KEY,
                latLng.getLatitude(),
                latLng.getLongitude())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WeatherData>() {
                    @Override
                    public void onCompleted() {
                        Log.i (WEATHERPRESENTER_LOG, "Get Weather Data complete.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e (WEATHERPRESENTER_LOG, "Get Weather Data meets error: "+e.getMessage());
                    }

                    @Override
                    public void onNext(WeatherData weatherData) {
                        if (null != mView) {
                            mView.updateWeatherUI(weatherData);
                        }
                    }
                });

    }

    public void fetchGeoLocationData (LatLng latLng) {
        Log.i(WEATHERPRESENTER_LOG, "LATLNG:"+ latLng);

        mGeoLocationService.getGeoLocationData(latLng,
                STR_GEOLOCATION_RESULT_TYPE,
                STR_GEOLOCATION_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GeoLocationData>() {
                    @Override
                    public void onCompleted() {
                        Log.i (WEATHERPRESENTER_LOG, "Get Geo Name complete.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i (WEATHERPRESENTER_LOG, "Get geo meets error: "+e.getMessage());
                    }

                    @Override
                    public void onNext(GeoLocationData geoLocationData) {
                        if (null != mView) {
                            mView.updateGeoLocationUI(geoLocationData);
                        }
                    }
                });
    }
}
