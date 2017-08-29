package com.volunteer.thc.volunteerapp.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.ChatAdapter;
import com.volunteer.thc.volunteerapp.model.Chat;

import java.util.ArrayList;

/**
 * Created by poppa on 25.08.2017.
 */

public class ChatFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ListView listChat;
    private ChatAdapter chatAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        listChat = (ListView) v.findViewById(R.id.list_chat);
        final ArrayList<Chat> array = new ArrayList<>();
        final ArrayList<Chat> arrayCopy = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), array);
        listChat.setAdapter(chatAdapter);

        mDatabase.child("conversation").orderByChild("receivedBy").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Chat chatData = data.getValue(Chat.class);
                    boolean change = false;
                    for (Chat chat : array) {
                        if (chat.getUuid().equals(chatData.getUuid())) {
                            change = true;
                            break;
                        }
                    }
                    if (!change) {
                        arrayCopy.add(chatData);
                        chatAdapter.add(chatData);
                        chatAdapter.notifyDataSetChanged();
                    }

                }

                listChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getContext(), ConversationActivity.class);
                        intent.putExtra("chat", arrayCopy.get(position));

                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return v;

    }


}
