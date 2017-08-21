package com.volunteer.thc.volunteerapp.util;

import java.text.SimpleDateFormat;
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
}
