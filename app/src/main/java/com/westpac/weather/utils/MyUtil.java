package com.westpac.weather.utils;
import java.math.BigDecimal;


/**
 * Created by yungang on 16/8/15.
 *
 */


public class MyUtil {

    public static double fahrenheit2Centigrade(double degree, int scale) {
        double d = (degree - 32) / 1.8;
        return new BigDecimal(d).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}