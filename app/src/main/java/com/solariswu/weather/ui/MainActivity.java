package com.solariswu.weather.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.solariswu.weather.R;
import com.solariswu.weather.adapters.DailyAdapter;
import com.solariswu.weather.adapters.HourlyAdapter;
import com.solariswu.weather.models.Datum_;
import com.solariswu.weather.models.GeoLocationData;
import com.solariswu.weather.models.WeatherData;
import com.solariswu.weather.services.WeatherPresenter;
import com.solariswu.weather.utils.Log;
import com.solariswu.weather.utils.MyUtil;
import com.solariswu.weather.utils.WeatherConsts;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;



public class MainActivity extends AppCompatActivity implements WeatherView {

    //Location services permission indication
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;

    //Weather forecast duration defines
    public static final int NUM_FORECAST_HOURS = 12;
    public static final int NUM_FORECAST_DAY = 7;

    //MVP presenter
    private WeatherPresenter mWeatherPresenter;

    //Butter knife variables
    Unbinder unbinder;

    //UI elements
    @BindView(R.id.tv_location)
    TextView mTVGeoLocation;

    @BindView(R.id.tv_currTemp)
    TextView mTVCurrTemp;

    @BindView(R.id.lv_Hourly)
    ListView mLVHourly;

    @BindView(R.id.lv_Daily)
    ListView mLVDaily;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        // Bind presenter
        mWeatherPresenter = new WeatherPresenter();
        mWeatherPresenter.onCreate(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                //Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // from android 23, needs user interaction for dangerous permissions grant
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mWeatherPresenter.requestLocation();
                }
                else {
                    showNoUserPermission();
                }
                return;
            }
            default:
                break;
        }
    }

    @Override
    public void initViews() {

    }

    @Override
    public void updateGeoLocationUI (GeoLocationData geoLocationData) {
        if (!geoLocationData.getResults().isEmpty() &&
                geoLocationData.getStatus().equals(WeatherConsts.GEOLOCATION_STATUS_OK)) {
            mTVGeoLocation.setText(geoLocationData.getResults().get(0).getFormattedAddress());
        }
        else {
            mTVGeoLocation.setText(R.string.str_unknowgeo);
        }
    }

    private void updateCurrentWeatherUI (WeatherData weatherData) {

        // set current temperature
        if (null != mTVCurrTemp) {
            Double dCurTemp = weatherData.getCurrently().getTemperature();
            Double dCentigrade = MyUtil.fahrenheit2Centigrade (dCurTemp, 0);

            String strCurTemp = dCentigrade.intValue()+"°C";
            mTVCurrTemp.setText(strCurTemp);
            mTVCurrTemp.setCompoundDrawablesRelativeWithIntrinsicBounds(null,MyUtil.mapIconStringToDrawable(this,
                    weatherData.getCurrently().getIcon()+"_128"),null,null);
        }

    }

    private void updateHourlyWeatherUI (WeatherData weatherData) {

        // set hourly list view
        if (null != mLVHourly) {
            List<Datum_> list = weatherData.getHourly().getData();

            //show 12 hours forecast
            Datum_[] array = new Datum_[NUM_FORECAST_HOURS];

            int i;
            for (i = 0; i<NUM_FORECAST_HOURS; i++) {
                array[i] = list.get(i);
            }

            mLVHourly.setAdapter(new HourlyAdapter(this, array, weatherData.getOffset()));
        }
    }

    private void updateDailyWeatherUI (WeatherData weatherData) {

        // set daily view list
        if (null != mLVDaily) {
            List<Datum_> list = weatherData.getDaily().getData();

            //show 7 days forecast
            Datum_[] array = new Datum_[NUM_FORECAST_DAY];

            int i;
            for (i = 0; i<NUM_FORECAST_DAY; i++) {
                array[i] = list.get(i);
            }

            mLVDaily.setAdapter(new DailyAdapter(this, array));
        }

    }

    @Override
    public void updateWeatherUI (WeatherData weatherData) {

        updateCurrentWeatherUI(weatherData);

        updateHourlyWeatherUI(weatherData);

        updateDailyWeatherUI(weatherData);

    }

    @Override
    public void showNoUserPermission () {
        Toast.makeText(this, "No User Permission.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void indicateGpsOn () {
        Toast.makeText(this, "Gps Enabled", Toast.LENGTH_SHORT).show();
        Log.i (WeatherConsts.WEATHERVIEW_LOG, "GPS on!");
    }

    @Override
    public void indicateGpsOff () {
        Toast.makeText(this, "Gps Disabled", Toast.LENGTH_SHORT).show();
        Log.i (WeatherConsts.WEATHERVIEW_LOG, "GPS off!");
    }
}
