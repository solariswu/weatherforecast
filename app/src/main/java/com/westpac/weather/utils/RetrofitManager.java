package com.westpac.weather.utils;

import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by yungang on 16/8/15.
 *
 */
public class RetrofitManager {
    private Retrofit mWeatherRetrofit;
    private OkHttpClient httpClient;
    private static RetrofitManager uniqueInstance = new RetrofitManager();

    private RetrofitManager() { httpClient = new OkHttpClient(); }
    public static RetrofitManager getInstance() {
        return uniqueInstance;
    }

    public Retrofit buildWeatherRetrofit(String SERVICE_API_BASE_URL) {
        if (null == mWeatherRetrofit) {

            Interceptor requestInterceptor = new Interceptor() {
                @Override
                public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    Log.i("com.westpac.weather", "request :" + request.urlString());
                    return chain.proceed(request);
                }
            };

            // setting log level
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            httpClient.interceptors().add(requestInterceptor);

            // add logging as last interceptor
            httpClient.interceptors().add(logging);

            mWeatherRetrofit = new Retrofit.Builder()
                .baseUrl(SERVICE_API_BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return mWeatherRetrofit;
    }
}
