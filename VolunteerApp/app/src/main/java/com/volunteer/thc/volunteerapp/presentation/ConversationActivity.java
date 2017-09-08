package com.volunteer.thc.volunteerapp.presentation;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.ConversationAdapter;
import com.volunteer.thc.volunteerapp.adaptor.EventVolunteersAdapter;
import com.volunteer.thc.volunteerapp.model.Chat;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventAcceptedUsersFragment;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventRegisteredUsersFragment;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by poppa on 26.08.2017.
 */

public class ConversationActivity extends AppCompatActivity {

    final ArrayList<Chat> arrayList = new ArrayList<>();
    public static String nameChat = null;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ConversationAdapter conversationAdapter;
    private EditText reply;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String idSent, idReceive;
    private Chat chatDefault;
    public static OrganiserSingleEventRegisteredUsersFragment fragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(nameChat);
        final RecyclerView conversation = (RecyclerView) findViewById(R.id.list_conversation);
        reply = (EditText) findViewById(R.id.input_conversation);
        ImageView sendMessage = (ImageView) findViewById(R.id.sendMessage);
        chatDefault = (Chat) getIntent().getSerializableExtra("chat");
        idSent = user.getUid();

        if (chatDefault.getSentBy().equals(idSent)) {
            idReceive = chatDefault.getReceivedBy();
        } else {
            idReceive = chatDefault.getSentBy();
        }


        conversation.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        conversation.setLayoutManager(linearLayoutManager);
        conversationAdapter = new ConversationAdapter(arrayList);
        conversation.setAdapter(conversationAdapter);


        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!reply.getText().toString().isEmpty()) {
                    Chat chat = new Chat(idSent, idReceive, reply.getText().toString(), chatDefault.getUuid(), Calendar.getInstance().getTimeInMillis(),false);
                    mDatabase.child("conversation").push().setValue(chat);
                    reply.setText(null);
                }
            }
        });


        mDatabase.child("conversation").orderByChild("uuid").equalTo(chatDefault.getUuid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chatData = dataSnapshot.getValue(Chat.class);
                conversationAdapter.addElement(chatData);
                conversation.getLayoutManager().scrollToPosition(conversationAdapter.getItemCount() - 1);
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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        MenuItem acceptVolunteer = menu.findItem(R.id.chat_accept_volunteer);
        final int position= getIntent().getIntExtra("position",0);
        String parentClass =  getIntent().getStringExtra("class");
        if(!parentClass.equals("adapter")) {
            acceptVolunteer.setVisible(false);
        }else {
            acceptVolunteer.setVisible(true);
            acceptVolunteer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    final AlertDialog.Builder alert = new AlertDialog.Builder(ConversationActivity.this);
                    alert.setTitle("Accept Volunteer?")
                            .setCancelable(true)
                            .setMessage("Are you sure you want to accept this volunteer?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    EventVolunteersAdapter.acceptVolunteer(position,ConversationActivity.this);
                                    fragment.adapter.notifyDataSetChanged();
                                    finish();

                                }
                            });


                    alert.show();

                    return false;
                }
            });

        }

        return super.onCreateOptionsMenu(menu);

    }


}
