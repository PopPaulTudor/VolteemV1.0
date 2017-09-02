package com.volunteer.thc.volunteerapp.util;

import android.util.Log;

/**
 * Created by Vlad on 25.08.2017.
 */
public class CalculateUtils {

    public static long calculateVolunteerExperience(int size, long nrOfDays) {
        long baseXp = 200;
        long bonusXp = size > 400 ? 400 : size;
        long totalXp = (baseXp + bonusXp) * nrOfDays;
        Log.d("Experience", "Total xp: " + totalXp);

        return totalXp;
    }

}
