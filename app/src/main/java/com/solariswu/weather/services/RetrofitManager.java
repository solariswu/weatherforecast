package com.solariswu.weather.services;


import com.solariswu.weather.utils.Log;
import com.solariswu.weather.utils.WeatherConsts;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;

import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;

/**
 * Created by yungang on 16/8/15.
 *
 */
public class RetrofitManager {
    private Retrofit mWeatherRetrofit;
    private Retrofit mGeoLocationRetrofit;
    private OkHttpClient httpClient;
    private static RetrofitManager uniqueInstance = new RetrofitManager();

    private RetrofitManager() {
        // setting log level
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // add logging as last interceptor
        httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Log.i(WeatherConsts.WEATHERSERVICE_LOG, "request :" + request.url().toString());
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(logging)
                .build();
    }

    public static RetrofitManager getInstance() {
        return uniqueInstance;
    }

    public Retrofit buildWeatherRetrofit(String SERVICE_API_BASE_URL) {
        if (null == mWeatherRetrofit) {
            mWeatherRetrofit = new Retrofit.Builder()
                    .baseUrl(SERVICE_API_BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }
        return mWeatherRetrofit;
    }

    public Retrofit buildGeoLocationRetrofit(String SERVICE_API_BASE_URL) {
        if (null == mGeoLocationRetrofit) {
            mGeoLocationRetrofit = new Retrofit.Builder()
                    .baseUrl(SERVICE_API_BASE_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
        }
        return mGeoLocationRetrofit;
    }
}
