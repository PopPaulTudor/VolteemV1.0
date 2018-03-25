package com.volunteer.thc.volunteerapp.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Created by Cristi on 8/26/2017.
 */

public class StartFirebaseServiceAtBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            context.startService(new Intent(context, FirebaseNewsService.class));
        }
    }
}
