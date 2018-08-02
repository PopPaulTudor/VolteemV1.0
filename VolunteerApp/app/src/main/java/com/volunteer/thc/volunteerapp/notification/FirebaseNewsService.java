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
import com.volunteer.thc.volunteerapp.model.ChatSingle;
import com.volunteer.thc.volunteerapp.model.Message;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;
import com.volunteer.thc.volunteerapp.presentation.chat.ConversationActivity;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventActivity;
import com.volunteer.thc.volunteerapp.presentation.volunteer.VolunteerSingleEventActivity;
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.Calendar;
import java.util.Random;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Cristi on 8/26/2017.
 */

public class FirebaseNewsService extends Service {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private SharedPreferences prefs;
    private Calendar date = Calendar.getInstance();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private int badgeCount = 0;

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


        mDatabase.child("news").orderByChild("receivedBy").equalTo(user.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (user != null) {
                    NewsMessage newsMessage = dataSnapshot.getValue(NewsMessage.class);

                    if (newsMessage.getContent().contains("A new contract has been uploaded for")) {
                        sendNews(newsMessage);
                        mDatabase.child("news/" + newsMessage.getNewsID()).setValue(null);

                    } else {
                        if (!newsMessage.isStarred() && (newsMessage.getExpireDate() + 604800000) < date.getTimeInMillis()) {
                            mDatabase.child("news/" + newsMessage.getNewsID()).setValue(null);
                        } else {
                            if (!newsMessage.isNotified()) {
                                if (prefs.getBoolean("notifications", true)) {
                                    sendNews(newsMessage);
                                    badgeCount = prefs.getInt("badgeCount", 0);
                                    ++badgeCount;
                                    prefs.edit().putInt("badgeCount", badgeCount).apply();
                                    ShortcutBadger.applyCount(getApplicationContext(), badgeCount);
                                }
                                mDatabase.child("news/" + dataSnapshot.getKey() + "/notified").setValue(true);
                                if (newsMessage.getType() == NewsMessage.EVENT_DELETED) {
                                    mDatabase.child("news/" + newsMessage.getNewsID()).setValue(null);
                                }
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
                final ChatSingle chatSingle = dataSnapshot.getValue(ChatSingle.class);
                if (!chatSingle.isReceived() && !chatSingle.getContent().equals(" ")) {
                    mDatabase.child("conversation/" + dataSnapshot.getKey() + "/received").setValue(true);
                    badgeCount = prefs.getInt("badgeCount", 0);
                    ++badgeCount;
                    prefs.edit().putInt("badgeCount", badgeCount).apply();
                    ShortcutBadger.applyCount(getApplicationContext(), badgeCount);
                    if (!ConversationActivity.idActive.equals(chatSingle.getUuid())) {
                        mDatabase.child("users").child("volunteers").child(chatSingle.getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    final Volunteer volunteer = dataSnapshot.getValue(Volunteer.class);
                                    final long ONE_MEGABYTE = 1024L * 1024L;
                                    storageRef.child("Photos").child("User").child(chatSingle.getSentBy()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {
                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            sendConversation(chatSingle.getContent(), volunteer.getFirstname() + " " + volunteer.getLastname(), chatSingle, ImageUtils.getCroppedBitmap(bitmap, getResources()));

                                        }
                                    });

                                } else {
                                    mDatabase.child("users").child("organisers").child(chatSingle.getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            final Organiser organiser = dataSnapshot.getValue(Organiser.class);

                                            final long ONE_MEGABYTE = 1024L * 1024L;
                                            storageRef.child("Photos").child("User").child(chatSingle.getSentBy()).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                    sendConversation(chatSingle.getContent(), organiser.getCompany(), chatSingle, ImageUtils.getCroppedBitmap(bitmap, getResources()));

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
                intent = new Intent(this, VolunteerSingleEventActivity.class);
                intent.putExtra(VolteemConstants.VOLUNTEER_SINGLE_ACTIVITY_CAME_FROM_KEY, 2);
                break;
            case NewsMessage.REGISTERED:
                intent = new Intent(this, OrganiserSingleEventActivity.class);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
        }
        intent.putExtra(VolteemConstants.INTENT_EVENT_ID, message.getEventID());
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

    private void sendConversation(String content, String title, Message chat, Bitmap largeIcon) {
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
