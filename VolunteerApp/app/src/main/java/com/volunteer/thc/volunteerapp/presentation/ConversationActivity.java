package com.volunteer.thc.volunteerapp.presentation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.volunteer.thc.volunteerapp.adaptor.ConversationAdapter;
import com.volunteer.thc.volunteerapp.model.Chat;

import java.util.ArrayList;

/**
 * Created by poppa on 26.08.2017.
 */

public class ConversationActivity extends AppCompatActivity {

    final ArrayList<Chat> arrayList = new ArrayList<>();
    public static String nameChat=null;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ConversationAdapter conversationAdapter;
    private EditText reply;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String idSent, idReceive;
    private Chat chatDefault;


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
                    Chat chat = new Chat(idSent, idReceive, reply.getText().toString(), chatDefault.getUuid());
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
}
