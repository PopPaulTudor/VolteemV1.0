package com.volunteer.thc.volunteerapp.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Chat;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.ConversationActivity;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;
import com.volunteer.thc.volunteerapp.util.ImageUtils;

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
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();


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
                                    sendNews(newsMessage.getContent(), "News");
                                }
                                mDatabase.child("news/" + dataSnapshot1.getKey() + "/notified").setValue(true);
                                if (newsMessage.getType() == NewsMessage.EVENT_DELETED) {
                                    mDatabase.child("news/" + newsMessage.getNewsID()).setValue(null);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("conversation").orderByChild("receivedBy").equalTo(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    final Chat chat = dataSnapshot1.getValue(Chat.class);
                    if (!chat.isReceived() && !chat.getContent().contains("You have been accepted to ")) {
                        mDatabase.child("conversation/" + dataSnapshot1.getKey() + "/received").setValue(true);

                        if (!ConversationActivity.idActive.equals(chat.getUuid())) {
                            mDatabase.child("users").child("volunteers").child(chat.getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        final Volunteer volunteer = dataSnapshot.getValue(Volunteer.class);

                                        final long ONE_MEGABYTE = 1024 * 1024;
                                        storageRef.child("Photos").child("User").child(user.getUid()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                sendConversation(chat.getContent(), volunteer.getFirstname() + " " + volunteer.getLastname(), chat, ImageUtils.getCroppedBitmap(bitmap, getResources()));

                                            }
                                        });

                                    } else {
                                        mDatabase.child("users").child("organisers").child(chat.getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                final Organiser organiser = dataSnapshot.getValue(Organiser.class);

                                                final long ONE_MEGABYTE = 1024 * 1024;
                                                storageRef.child("Photos").child("User").child(user.getUid()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                    @Override
                                                    public void onSuccess(byte[] bytes) {
                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                        sendConversation(chat.getContent(), organiser.getCompany(), chat, ImageUtils.getCroppedBitmap(bitmap, getResources()));

                                                    }
                                                });


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
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


    private void sendNews(String content, String title) {


        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        final Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.volunteer_logo)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .build();


        notificationManager.notify(new Random().nextInt(), notification);
    }


    private void sendConversation(String content, String title, Chat chat, Bitmap largeIcon) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, ConversationActivity.class);

        intent.putExtra("chat", chat);
        intent.putExtra("class", "firebase");
        ConversationActivity.nameChat = title;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        final Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.volunteer_logo)
                .setLargeIcon(largeIcon)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .build();


        notificationManager.notify(new Random().nextInt(), notification);
    }


}
