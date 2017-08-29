package com.volunteer.thc.volunteerapp.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Cristi on 8/26/2017.
 */

public class StartFirebaseServiceAtBoot extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, FirebaseNewsService.class));
        ///TODO: Start other services too, such as chats listener service
    }
}
