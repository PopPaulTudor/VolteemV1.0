package com.volunteer.thc.volunteerapp.util;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.List;

/**
 * Created by poppa on 16.08.2017.
 */
public final class PermissionUtil {

    public static boolean isStorageReadPermissionGranted(Context context) {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isStorageWritePermissionGranted(Context context) {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED;
    }


    public static boolean isRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (context.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }
        return false;
    }

}
