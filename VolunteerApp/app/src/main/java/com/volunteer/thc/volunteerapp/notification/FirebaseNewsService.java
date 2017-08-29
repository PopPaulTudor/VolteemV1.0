package com.volunteer.thc.volunteerapp.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Cristi on 8/26/2017.
 */

public class FirebaseNewsService extends Service {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private SharedPreferences prefs;
    private Calendar date = Calendar.getInstance();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("FirebaseService", "created");
        prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mDatabase.child("news").orderByChild("receivedBy").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (user != null) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        NewsMessage newsMessage = dataSnapshot1.getValue(NewsMessage.class);
                        if (!newsMessage.isStarred() && (newsMessage.getExpireDate() + 604800000) < date.getTimeInMillis()) {
                            mDatabase.child("news/" + newsMessage.getNewsID()).setValue(null);
                        } else {
                            if (!dataSnapshot1.child("notified").getValue(Boolean.class)) {
                                if (prefs.getBoolean("notifications", true)) {
                                    sendNotification(newsMessage);
                                }
                                mDatabase.child("news/" + dataSnapshot1.getKey() + "/notified").setValue(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("FirebaseService", "running");
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendNotification(NewsMessage message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle("News")
                .setContentText(message.getContent())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.volunteer_logo)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .build();
        notificationManager.notify(new Random().nextInt(), notification);
    }
}
