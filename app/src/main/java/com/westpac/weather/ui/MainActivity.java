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

    //DARK Sky weather API_KEY, url and data notification ID
    public static final String STR_WEATHER_API_KEY     = "677bb1722471bc47d236bc195384c273";
    public static final String STR_WEATHER_URL         = "https://api.darksky.net/";
    public static final String ACTION_WEATHER_DATA_NOTIFY = "ACTION_WEATHER_DATA_NOTIFY";


    //Google GEOLOCATION API_KEY, url and data notification ID
    public static final String STR_GEOLOCATION_API_KEY     = "AIzaSyCsPFEM6kbaJSdnCugIfexgeo9w_7zSnbA";
    public static final String STR_GEOLOCATION_URL         = "https://maps.googleapis.com/";
    public static final String STR_GEOLOCATION_RESULT_TYPE = "country|locality";
    public static final String ACTION_GEOLOCATION_DATA_NOTIFY = "ACTION_GEOLOCATION_DATA_NOTIFY";


    //Weather forecast duration defines
    public static final int NUM_FORECAST_HOURS = 12;
    public static final int NUM_FORECAST_DAY = 7;


    //Log Identification String
    public static final String WEATHERREPORT_LOG = "WEATHERPORT";


    //Data storages
    private WeatherData mWeatherData;
    private BroadcastReceiver mReceiver;
    private AsyncTask<String, Void, Long> mDataFetchTask;


    //Service variables
    LocationManager mlocManager;
    LocationListener mlocListener;
    RetrofitService mGeoLocationService;


    //Location variables
    private double mLatitude;
    private double mLongitude;


    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1001;


    //View variables
    Unbinder unbinder;

    @BindView(R.id.tv_location)
    TextView mTVGeoLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = ButterKnife.bind(this);

        // create receiver to get weather data update
        mReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String intentAction = intent.getAction();

                if (intentAction.equals(MainActivity.ACTION_WEATHER_DATA_NOTIFY) &&
                        null !=mWeatherData)
                {
                    updateWeatherUI(mWeatherData);
                }
            }
        };

        registerReceiver(mReceiver, new IntentFilter(MainActivity.ACTION_WEATHER_DATA_NOTIFY));

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

        requestLocation();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
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

    private void updateGeoLocation (double latitude, double longitude) {

        Log.i(WEATHERREPORT_LOG, "LAT:"+latitude+" LNG:"+longitude);

        mGeoLocationService.getGeoLocationData(new LatLng(latitude, longitude),
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
            String formattedAddress[];
            formattedAddress = geoLocationData.getResults().get(0).getFormattedAddress().split(",");
            Log.i (WEATHERREPORT_LOG, "FormattedAddress: " +
                    geoLocationData.getResults().get(0).getFormattedAddress());
            mTVGeoLocation.setText(geoLocationData.getResults().get(0).getFormattedAddress());
           // mTVGeoLocation.setText(formattedAddress[1]+"/"+formattedAddress[2]);
        }
    }

    private void updateWeatherUI (WeatherData weatherData) {

        // Set current weather
        ImageView imageView = (ImageView) this.findViewById(R.id.bigweathericon);
        if (null != imageView)
            imageView.setImageDrawable(MyUtil.mapIconStringToDrawable(this,
                    weatherData.getCurrently().getIcon()+"_128"));

        // set current temperature
        Double dCurTemp = weatherData.getCurrently().getTemperature();
        Double dCentigrade = MyUtil.fahrenheit2Centigrade (dCurTemp, 0);

        TextView currTemp = (TextView) this.findViewById(R.id.currTemp);
        String strCurTemp = dCentigrade.intValue()+"°C";

        if (null != currTemp)
            currTemp.setText(strCurTemp);

        // set hourly list view
        ListView lvHourly = (ListView) findViewById(R.id.lvHourly);
        if (null != lvHourly) {
            List<Datum_> list = weatherData.getHourly().getData();

            //show 12 hours forecast
            Datum_[] array = new Datum_[NUM_FORECAST_HOURS];

            int i;
            for (i = 0; i<NUM_FORECAST_HOURS; i++) {
                array[i] = list.get(i);
            }

            lvHourly.setAdapter(new HourlyAdapter(this, array, mWeatherData.getOffset()));
        }

        // set daily view list
        ListView lvDaily = (ListView) findViewById(R.id.lvDaily);
        if (null != lvDaily) {
            List<Datum_> list = weatherData.getDaily().getData();

            //show 7 days forecast
            Datum_[] array = new Datum_[NUM_FORECAST_DAY];

            int i;
            for (i = 0; i<NUM_FORECAST_DAY; i++) {
                array[i] = list.get(i);
            }

            lvDaily.setAdapter(new DailyAdapter(this, array));
        }
    }

    private void requestLocation () {
        //use location manager service to get Latitude and Longitude info.
        try {
            if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    mLatitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                    mDataFetchTask = new onRequestWeather().execute();
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

            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();

            mDataFetchTask = new onRequestWeather().execute();

            //update GEO Location info with the Latitude & Longitude
            updateGeoLocation(mLatitude, mLongitude);
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


    private class onRequestWeather extends AsyncTask<String, Void, Long> {

        @Override
        protected Long doInBackground(String... params) {
            if(isCancelled())
                return 0L;

            try {
                Retrofit retrofit = RetrofitManager.getInstance().buildWeatherRetrofit(STR_WEATHER_URL);
                RetrofitService retrofitService = retrofit.create(RetrofitService.class);

                // Call Retrofit service to fetch Weather data from server URL
                Call<WeatherData> call = retrofitService.getWeatherData(STR_WEATHER_API_KEY,
                        mLatitude,
                        mLongitude);
                Response<WeatherData> responseResponse;
                responseResponse = call.execute();
                if (responseResponse.isSuccessful()) {
                    mWeatherData = responseResponse.body();
                    Intent data = new Intent(ACTION_WEATHER_DATA_NOTIFY);
                    sendBroadcast(data);
                }
            }
            catch (Exception exc) {
                exc.printStackTrace();
            }
            return 0L;
        }

        @Override
        protected void onPostExecute(Long result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}
