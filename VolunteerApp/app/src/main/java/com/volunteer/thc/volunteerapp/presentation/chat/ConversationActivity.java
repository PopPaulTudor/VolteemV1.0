package com.volunteer.thc.volunteerapp.presentation.chat;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.ConversationAdapter;
import com.volunteer.thc.volunteerapp.model.ChatGroup;
import com.volunteer.thc.volunteerapp.model.ChatSingle;
import com.volunteer.thc.volunteerapp.model.Message;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventRegisteredUsersFragment;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by poppa on 26.08.2017.
 */

public class ConversationActivity extends AppCompatActivity {

    public static String nameChat = null;
    public static String idActive = "";
    public static OrganiserSingleEventRegisteredUsersFragment fragment;
    final ArrayList<Message> arrayList = new ArrayList<>();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ConversationAdapter conversationAdapter;
    private EditText reply;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String idSent, idReceive;
    private ChatSingle chatSingle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(nameChat);
        final RecyclerView conversation = findViewById(R.id.list_conversation);
        reply = findViewById(R.id.input_conversation);


        ImageView sendMessage = (ImageView) findViewById(R.id.sendMessage);
        chatSingle = (ChatSingle) getIntent().getSerializableExtra("chat");
        idActive = chatSingle.getUuid();

        idSent = user.getUid();

        if (chatSingle != null) {
            if (chatSingle.getSentBy().equals(idSent))
                idReceive = chatSingle.getReceivedBy();
            else
                idReceive = chatSingle.getSentBy();
        }

        conversation.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        conversation.setLayoutManager(linearLayoutManager);
        conversationAdapter = new ConversationAdapter(arrayList, getApplicationContext());
        conversation.setAdapter(conversationAdapter);
        conversation.setHasFixedSize(false);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversation.getLayoutManager().scrollToPosition(conversationAdapter.getItemCount() - 1);
                if (!reply.getText().toString().isEmpty()) {

                    ChatSingle chatSingle = new ChatSingle(idSent, idReceive, reply.getText().toString(), ConversationActivity.this.chatSingle.getUuid(), Calendar.getInstance().getTimeInMillis(), false);
                    mDatabase.child("conversation").child("single").push().setValue(chatSingle);
                    reply.setText(null);

                }

            }
        });


        mDatabase.child("conversation").child("single").orderByChild("uuid").equalTo(chatSingle.getUuid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatSingle chatSingleData = dataSnapshot.getValue(ChatSingle.class);
                conversationAdapter.addElement(chatSingleData);
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        idActive = "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        MenuItem acceptVolunteer = menu.findItem(R.id.chat_accept_volunteer);
        final String positionId = getIntent().getStringExtra("id");
        String parentClass = getIntent().getStringExtra("class");
        if (!parentClass.equals("adapter")) {
            acceptVolunteer.setVisible(false);
        } else {
            acceptVolunteer.setVisible(true);
            acceptVolunteer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    final AlertDialog.Builder alert = new AlertDialog.Builder(ConversationActivity.this);
                    alert.setTitle("Accept Volunteer?")
                            .setCancelable(true)
                            .setMessage("Are you sure you want to accept this volunteer?")
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    OrganiserSingleEventRegisteredUsersFragment.adapter.acceptVolunteer(positionId, ConversationActivity.this);
                                    OrganiserSingleEventRegisteredUsersFragment.adapter.notifyDataSetChanged();
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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        idActive = "";
    }
}
