package com.solariswu.weather.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.solariswu.weather.R;
import com.solariswu.weather.models.Datum_;
import com.solariswu.weather.utils.MyUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yungang on 16/8/15.
 *
 */
public class HourlyAdapter extends ArrayAdapter<Datum_> {
    private Context mContext;
    // timezone offset got from weather data source
    private Integer mTimeOffset;

    public HourlyAdapter(Context context, Datum_[] objects, Integer offset) {
        super(context, R.layout.hourlyitems_list, R.id.short_desc, objects);
        mContext = context;
        mTimeOffset = offset;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);

        //Set weather icons
        ImageView imageView = (ImageView) rootView.findViewById(R.id.weather_ico);
        imageView.setImageDrawable(MyUtil.mapIconStringToDrawable(mContext,
                getItem(position).getIcon()));

        //Set weather summary
        ((TextView) rootView.findViewById(R.id.short_desc)).setText(getItem(position).getSummary());

        //Set hourly time value and format
        Long time = getItem(position).getTime() - mTimeOffset *3600;

        Date dateNtp = new Date(time*1000);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.US);
        String dateFormatNtp = format.format(dateNtp);

        ((TextView) rootView.findViewById(R.id.hourly_time)).setText(dateFormatNtp);

        return rootView;
    }
}
