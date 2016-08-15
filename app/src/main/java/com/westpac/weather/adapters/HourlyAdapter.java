package com.westpac.weather.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.westpac.weather.R;
import com.westpac.weather.models.Datum;
import com.westpac.weather.models.Datum_;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yungang on 16/8/15.
 *
 */
public class HourlyAdapter extends ArrayAdapter<Datum_> {
    public HourlyAdapter(Context context, Datum_[] objects) {
        super(context, R.layout.hourlyitems_list, R.id.weather_ico, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);

        //setting up related data from the data structure to corresponding views
        //String strIco = getItem(position).getIcon();
        ((TextView) rootView.findViewById(R.id.weather_ico)).setText(getItem(position).getIcon());
        ((TextView) rootView.findViewById(R.id.short_desc)).setText(getItem(position).getSummary());

        Long time = getItem(position).getTime();

        Date dateNtp = new Date(time*1000);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        String dateFormatNtp = format.format(dateNtp);

        ((TextView) rootView.findViewById(R.id.hourly_time)).setText(dateFormatNtp);


    return rootView;
    }
}
