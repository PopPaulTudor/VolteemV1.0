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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.ChangeEvent;
import com.volunteer.thc.volunteerapp.model.Chat;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.ConversationActivity;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventActivity;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerSingleEventActivity;
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


        mDatabase.child("changes").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (user != null) {
                    final ChangeEvent changeEvent = dataSnapshot.getValue(ChangeEvent.class);
                    if (!changeEvent.isNotified()) {
                        mDatabase.child("events/" + changeEvent.getEvent().getEventID() + "/users").orderByChild("status").equalTo("pending").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                sendChangesEvent(changeEvent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mDatabase.child("changes/" + dataSnapshot.getKey() + "/notified").setValue(true);

                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("news").orderByChild("receivedBy").equalTo(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (user != null) {
                    NewsMessage newsMessage = dataSnapshot.getValue(NewsMessage.class);
                    if (!newsMessage.isStarred() && (newsMessage.getExpireDate() + 604800000) < date.getTimeInMillis()) {
                        mDatabase.child("news/" + newsMessage.getNewsID()).setValue(null);
                    } else {
                        if (!newsMessage.isNotified()) {
                            if (prefs.getBoolean("notifications", true)) {
                                sendNews(newsMessage);
                            }
                            mDatabase.child("news/" + dataSnapshot.getKey() + "/notified").setValue(true);
                            if (newsMessage.getType() == NewsMessage.EVENT_DELETED) {
                                mDatabase.child("news/" + newsMessage.getNewsID()).setValue(null);
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("NewsService", databaseError.getMessage());
            }
        });

        mDatabase.child("conversation").orderByChild("receivedBy").equalTo(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Chat chat = dataSnapshot.getValue(Chat.class);
                if (!chat.isReceived() && !chat.getContent().contains("You have been accepted to ")) {
                    mDatabase.child("conversation/" + dataSnapshot.getKey() + "/received").setValue(true);

                    if (!ConversationActivity.idActive.equals(chat.getUuid())) {
                        mDatabase.child("users").child("volunteers").child(chat.getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    final Volunteer volunteer = dataSnapshot.getValue(Volunteer.class);
                                    final long ONE_MEGABYTE = 1024 * 1024;
                                    storageRef.child("Photos").child("User").child(chat.getSentBy()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
                                            storageRef.child("Photos").child("User").child(chat.getSentBy()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

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

    private void sendNews(NewsMessage message) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent;
        switch (message.getType()) {
            case NewsMessage.ACCEPT:
                prefs.edit().putInt("cameFrom", 2).apply();
                intent = new Intent(this, VolunteerSingleEventActivity.class);
                break;
            case NewsMessage.REGISTERED:
                intent = new Intent(this, OrganiserSingleEventActivity.class);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra("eventID", message.getEventID());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        final Notification notification = new Notification.Builder(this)
                .setContentTitle("News")
                .setContentText(message.getContent())
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


    private void sendChangesEvent(ChangeEvent changeEvent) {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, VolunteerSingleEventActivity.class);

        intent.putExtra("eventID", changeEvent.getEvent().getEventID());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        final Notification notification = new Notification.Builder(this)
                .setContentTitle(changeEvent.getTitle())
                .setContentText(changeEvent.getContent())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.volunteer_logo)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .build();

        notificationManager.notify(new Random().nextInt(), notification);
    }
}
