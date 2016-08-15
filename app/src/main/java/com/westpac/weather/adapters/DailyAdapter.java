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
import com.westpac.weather.utils.MyUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yungang on 16/8/15.
 *
 */
public class DailyAdapter extends ArrayAdapter<Datum_> {
    public DailyAdapter(Context context, Datum_[] objects) {
        super(context, R.layout.dailyitems_list, R.id.daily_ico, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);

        //setting up related data from the data structure to corresponding views
        //String strIco = getItem(position).getIcon();
        ((TextView) rootView.findViewById(R.id.daily_ico)).setText(getItem(position).getIcon());
        ((TextView) rootView.findViewById(R.id.daily_desc)).setText(getItem(position).getSummary());

        Long time = getItem(position).getTime();

        Double dTempMin = MyUtil.fahrenheit2Centigrade(getItem(position).getTemperatureMin(),0);
        Double dTempMax = MyUtil.fahrenheit2Centigrade(getItem(position).getTemperatureMax(),0);

        Date dateNtp = new Date(time*1000);
        SimpleDateFormat format = new SimpleDateFormat("E:", Locale.US);
        String dateFormatNtp = format.format(dateNtp)+ "  "+
                dTempMin.intValue() +"/"+ dTempMax.intValue()+"°C";

        ((TextView) rootView.findViewById(R.id.daily_date)).setText(dateFormatNtp);

        return rootView;
    }
}
