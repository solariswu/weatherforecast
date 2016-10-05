package com.westpac.weather.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.westpac.weather.R;
import com.westpac.weather.adapters.DailyAdapter;
import com.westpac.weather.adapters.HourlyAdapter;
import com.westpac.weather.models.Datum_;
import com.westpac.weather.models.GeoLocationData;
import com.westpac.weather.models.LatLng;
import com.westpac.weather.models.WeatherData;
import com.westpac.weather.services.RetrofitService;
import com.westpac.weather.utils.MyUtil;
import com.westpac.weather.utils.RetrofitManager;


import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Path;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    //Log Identification String
    public static final String WEATHERREPORT_LOG = "WEATHERPORT";

    //Retrofit Service variables
    private RetrofitService mGeoLocationService;
    private RetrofitService mWeatherService;

    //DARK Sky weather API_KEY, url and data notification ID
    public static final String STR_WEATHER_API_KEY     = "677bb1722471bc47d236bc195384c273";
    public static final String STR_WEATHER_URL         = "https://api.darksky.net/";

    //Google GEOLOCATION API_KEY, url and data notification ID
    public static final String STR_GEOLOCATION_API_KEY     = "AIzaSyCsPFEM6kbaJSdnCugIfexgeo9w_7zSnbA";
    public static final String STR_GEOLOCATION_URL         = "https://maps.googleapis.com/";
    public static final String STR_GEOLOCATION_RESULT_TYPE = "country|locality";

    //Location services permission indication
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;

    //GPS Location Service variables
    private LocationManager mlocManager;
    private LocationListener mlocListener;

    //Weather forecast duration defines
    public static final int NUM_FORECAST_HOURS = 12;
    public static final int NUM_FORECAST_DAY = 7;

    //Butterknife variables
    Unbinder unbinder;

    //UI elements
    @BindView(R.id.tv_location)
    TextView mTVGeoLocation;

    @BindView(R.id.iv_bigweather)
    ImageView mIVBigWeather;

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

        // setup location update listener
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mlocListener = new WFLocationListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Request permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }

        Retrofit geoLocationRetrofit = RetrofitManager.getInstance()
                .buildGeoLocationRetrofit(STR_GEOLOCATION_URL);

        mGeoLocationService = geoLocationRetrofit.create(RetrofitService.class);

        Retrofit weatherRetrofit = RetrofitManager.getInstance()
                .buildWeatherRetrofit(STR_WEATHER_URL);

        mWeatherService = weatherRetrofit.create(RetrofitService.class);

        requestLocation();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // from android 23, needs user interaction for dangerous permissions grant
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                } else {
                    Toast.makeText(this, "No Permission Granted.", Toast.LENGTH_LONG).show();
                }
                return;
            }
            default:
                break;
        }
    }

    private void fetchGeoLocation (LatLng latLng) {

        Log.i(WEATHERREPORT_LOG, "LATLNG:"+ latLng);

        mGeoLocationService.getGeoLocationData(latLng,
                STR_GEOLOCATION_RESULT_TYPE,
                STR_GEOLOCATION_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GeoLocationData>() {
                    @Override
                    public void onCompleted() {
                      /*  Toast.makeText(MainActivity.this, "Get Geo Name complete.",
                                Toast.LENGTH_LONG)
                                .show();*/
                        Log.i (WEATHERREPORT_LOG, "Get Geo Name complete.");
                    }

                    @Override
                    public void onError(Throwable e) {
                       /* Toast.makeText(MainActivity.this, "Get Geo Name error.", Toast.LENGTH_LONG)
                                .show();*/
                        Log.i (WEATHERREPORT_LOG, "Get geo meets error: "+e.getMessage());
                    }

                    @Override
                    public void onNext(GeoLocationData geoLocationData) {
                        updateGeoLocationUI(geoLocationData);
                    }
                });
    }

    private void updateGeoLocationUI (GeoLocationData geoLocationData) {
        if (!geoLocationData.getResults().isEmpty()) {
            mTVGeoLocation.setText(geoLocationData.getResults().get(0).getFormattedAddress());
        }
    }

    private void fetchWeatherData (LatLng latLng) {

        Log.i(WEATHERREPORT_LOG, "Fetch weather Data LATLNG:"+ latLng);

        mWeatherService.getWeatherData(STR_WEATHER_API_KEY,
                latLng.getLatitude(),
                latLng.getLongitude())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<WeatherData>() {
                    @Override
                    public void onCompleted() {
                        Log.i (WEATHERREPORT_LOG, "Get Weather Data complete.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e (WEATHERREPORT_LOG, "Get Weather Data meets error: "+e.getMessage());
                    }

                    @Override
                    public void onNext(WeatherData weatherData) {
                        updateWeatherUI(weatherData);
                    }
                });
    }

    private void updateWeatherUI (WeatherData weatherData) {

        // Set current weather
        if (null != mIVBigWeather)
            mIVBigWeather.setImageDrawable(MyUtil.mapIconStringToDrawable(this,
                    weatherData.getCurrently().getIcon()+"_128"));

        // set current temperature
        if (null != mTVCurrTemp) {
            Double dCurTemp = weatherData.getCurrently().getTemperature();
            Double dCentigrade = MyUtil.fahrenheit2Centigrade (dCurTemp, 0);

            String strCurTemp = dCentigrade.intValue()+"°C";
            mTVCurrTemp.setText(strCurTemp);
        }

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

    private void requestLocation () {
        //use location manager service to get Latitude and Longitude info.
        try {
            if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    fetchWeatherData(latLng);

                    fetchWeatherData(latLng);
                }
                mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1000, mlocListener);
            } else {
                mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1000, mlocListener);
            }
        }
        catch (SecurityException e) {
            Toast.makeText(this, "No User Permission.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    private class WFLocationListener implements LocationListener {

        private Context mContext;

        public WFLocationListener(Context context) {
            this.mContext = context;
        }

        @Override
        public void onLocationChanged(Location location) {
            // Start to fetch weather data from server

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            //get the weather data by the Latitude & Longitude value
            fetchWeatherData(latLng);

            //get GEO Location info with the Latitude & Longitude value
            fetchGeoLocation(latLng);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(mContext.getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override

        public void onProviderEnabled(String provider) {
            Toast.makeText(mContext.getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

    }

}
