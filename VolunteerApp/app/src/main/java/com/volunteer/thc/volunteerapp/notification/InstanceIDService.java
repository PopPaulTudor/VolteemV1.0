package com.volunteer.thc.volunteerapp.notification;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by poppa on 23.08.2017.
 */

public class InstanceIDService extends FirebaseInstanceIdService {



    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        sendRegistrationToServer(refreshedToken);
    }


    private void sendRegistrationToServer(String token) {
        // Add custom implementation, as needed.
    }

}
