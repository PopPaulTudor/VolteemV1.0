package com.volunteer.thc.volunteerapp.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by poppa on 02.08.2017.
 */

public final class CalendarUtil {


    public static String getStringDateFromMM(long date){

        Calendar calendar= Calendar.getInstance();
        calendar.setTimeInMillis(date);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setTimeZone(calendar.getTimeZone());
        return dateFormat.format(calendar.getTime());
    }
}
