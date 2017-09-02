package com.volunteer.thc.volunteerapp.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by poppa on 02.08.2017.
 */
public final class CalendarUtil {

    public static String getStringDateFromMM(long millis) {
        Date date = new Date(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(date);
    }

    public static String getHourFromLong(long milis){

        Calendar calendar= Calendar.getInstance();
        calendar.setTimeInMillis(milis);

        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int minute=calendar.get(Calendar.MINUTE);

        return  hour+":"+minute;
    }

}
