package com.westpac.weather.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
public class HourlyAdapter extends ArrayAdapter<Datum_> {
    private Context mContext;
    private Integer mTimeOffset;

    public HourlyAdapter(Context context, Datum_[] objects, Integer offset) {
        super(context, R.layout.hourlyitems_list, R.id.short_desc, objects);
        mContext = context;
        mTimeOffset = offset;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = super.getView(position, convertView, parent);

        //Set weather icons
        ImageView imageView = (ImageView) rootView.findViewById(R.id.weather_ico);
        imageView.setImageDrawable(MyUtil.mapIconStringToDrawable(mContext,
                getItem(position).getIcon()));

        //Set weather summary
        ((TextView) rootView.findViewById(R.id.short_desc)).setText(getItem(position).getSummary());

        //Set hourly time
        Long time = getItem(position).getTime() - mTimeOffset *3600;

        Date dateNtp = new Date(time*1000);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a", Locale.US);
        String dateFormatNtp = format.format(dateNtp);

        ((TextView) rootView.findViewById(R.id.hourly_time)).setText(dateFormatNtp);

        return rootView;
    }
}
