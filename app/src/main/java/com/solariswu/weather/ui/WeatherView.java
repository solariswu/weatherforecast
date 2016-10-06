package com.solariswu.weather.ui;

import com.solariswu.weather.models.GeoLocationData;
import com.solariswu.weather.models.WeatherData;

/**
 * Created by solariswu on 16/10/5.
 *
 */

public interface WeatherView {

    void initViews ();

    void updateGeoLocationUI (GeoLocationData geoLocationData);

    void updateWeatherUI (WeatherData weatherData);

    void showNoUserPermission ();

    void indicateGpsOn ();

    void indicateGpsOff ();

}
