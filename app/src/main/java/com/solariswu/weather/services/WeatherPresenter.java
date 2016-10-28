package com.solariswu.weather.services;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.solariswu.weather.models.GeoLocationData;
import com.solariswu.weather.models.LatLng;
import com.solariswu.weather.models.WeatherData;
import com.solariswu.weather.ui.WeatherView;
import com.solariswu.weather.utils.Log;
import com.solariswu.weather.utils.WeatherConsts;

import javax.annotation.Nullable;

import retrofit2.Retrofit;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.solariswu.weather.utils.WeatherConsts.WEATHERPRESENTER_LOG;

/**
 * Created by solariswu on 16/10/5.
 *
 */

public class WeatherPresenter {

    //Retrofit Service variables
    private RetrofitService mGeoLocationService;
    private RetrofitService mWeatherService;

    //GPS Location Service variables
    private LocationManager mlocManager;
    private LocationListener mlocListener;

    @Nullable
    private WeatherView mView;

    public void onCreate(@NonNull WeatherView weatherViewView) {
        mView = weatherViewView;

        // call View's implemented methods
        mView.initViews();

        // prepare the retrofit services
        Retrofit geoLocationRetrofit = RetrofitManager.getInstance()
                .buildGeoLocationRetrofit(WeatherConsts.GEOLOCATION_URL);

        mGeoLocationService = geoLocationRetrofit.create(RetrofitService.class);

        Retrofit weatherRetrofit = RetrofitManager.getInstance()
                .buildWeatherRetrofit(WeatherConsts.WEATHER_URL);

        mWeatherService = weatherRetrofit.create(RetrofitService.class);

        // setup location update listener
        mlocManager = (LocationManager)
                ((Context) weatherViewView).getSystemService(Context.LOCATION_SERVICE);

        mlocListener = new WFLocationListener();

    }

    private void fetchWeatherData (LatLng latLng) {
        Log.i(WEATHERPRESENTER_LOG, "Fetch weather Data LATLNG:"+ latLng);

        mWeatherService.getWeatherData(WeatherConsts.WEATHER_API_KEY,
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

    private void fetchGeoLocationData (LatLng latLng) {
        Log.i(WEATHERPRESENTER_LOG, "LATLNG:"+ latLng);

        mGeoLocationService.getGeoLocationData(latLng,
                WeatherConsts.GEOLOCATION_RESULT_TYPE,
                WeatherConsts.GEOLOCATION_API_KEY)
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

    public void requestLocation () {
        //use location manager service to get Latitude and Longitude info.
        try {
            if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    fetchWeatherData(latLng);

                    fetchGeoLocationData(latLng);
                }
                mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 1000, mlocListener);
            } else {
                mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        0, 1000, mlocListener);
            }
        }
        catch (SecurityException e) {
            if (null != mView) {
                mView.showNoUserPermission();
            }
        }
    }

    private class WFLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // Start to fetch weather data from server

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            //get the weather data by the Latitude & Longitude value
            fetchWeatherData(latLng);

            //get GEO Location info with the Latitude & Longitude value
            fetchGeoLocationData(latLng);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            if (null != mView) {
                mView.indicateGpsOff();
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (null != mView) {
                mView.indicateGpsOn();
            }
        }
    }
}
