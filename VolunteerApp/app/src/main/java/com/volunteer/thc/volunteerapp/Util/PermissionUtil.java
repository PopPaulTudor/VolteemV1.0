package com.volunteer.thc.volunteerapp.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/**
 * Created by poppa on 16.08.2017.
 */

public final  class PermissionUtil {

    public  static boolean isStoragePermissionGranted(Context context) {
        return Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
