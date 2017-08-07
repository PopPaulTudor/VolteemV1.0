package com.volunteer.thc.volunteerapp.Util;

import java.util.Calendar;

/**
 * Created by poppa on 02.08.2017.
 */

public final class CalendarUtil {


    public static String getStringDateFromMM(long date){

       Calendar calendar= Calendar.getInstance();
        calendar.setTimeInMillis(date);
        calendar.setTimeInMillis(calendar.getTimeInMillis()-86400000L);
        String displayDate= calendar.get(Calendar.DAY_OF_WEEK)+"/"+calendar.get(Calendar.MONTH)+"/"+calendar.get(Calendar.YEAR);

        if(calendar.get(Calendar.DAY_OF_MONTH)<10)return 0+displayDate;
        else return displayDate;
    }

}
