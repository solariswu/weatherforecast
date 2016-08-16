package com.westpac.weather.utils;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.westpac.weather.R;

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

    public static Drawable mapIconStringToDrawable (Context context, String iconStr) {

        String uri = "@drawable/"+iconStr.replaceAll("-", "_");

        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());

        return context.getResources().getDrawable(imageResource);

    }
}