package com.solariswu.weather.models;

import java.util.Locale;

/**
 * Created by Yungang Wu on 16/10/5.
 *
 */

public class LatLng {
    private double mLatitude;
    private double mLongitude;

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%.5f,%.5f", mLatitude, mLongitude);
    }

    public LatLng(double mLatitude, double mLongitude) {
        this.mLatitude = mLatitude;
        this.mLongitude = mLongitude;
    }

    public double getLatitude () { return mLatitude; }

    public double getLongitude () { return mLongitude; }

}
