package com.volunteer.thc.volunteerapp.presentation.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.ChatAdapter;
import com.volunteer.thc.volunteerapp.model.ChatSingle;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by poppa on 25.08.2017.
 */
public class ChatFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ListView listChat;
    private ChatAdapter chatAdapter;
    private TextView noChatTextView;
    final ArrayList<ChatSingle> array = new ArrayList<>();
    final ArrayList<ChatSingle> arrayCopy = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        listChat = (ListView) v.findViewById(R.id.list_chat);

        chatAdapter = new ChatAdapter(getContext(), array);
        listChat.setAdapter(chatAdapter);
        noChatTextView = v.findViewById(R.id.no_chat_text);

        mDatabase.child("conversation").orderByChild("receivedBy").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noChatTextView.setVisibility(View.GONE);
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ChatSingle chatData = data.getValue(ChatSingle.class);
                    boolean change = false;
                    for (ChatSingle chat : arrayCopy) {
                        if (chat.getSentBy().equals(chatData.getSentBy())) {
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
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        mDatabase.child("users").child("volunteers").child(arrayCopy.get(position).getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Volunteer volunteer = dataSnapshot.getValue(Volunteer.class);
                                    ConversationActivity.nameChat = volunteer.getFirstname() + " " + volunteer.getLastname();
                                    Intent intent = new Intent(getContext(), ConversationActivity.class);
                                    intent.putExtra("chat", arrayCopy.get(position));
                                    intent.putExtra("class", "fragment");
                                    startActivity(intent);

                                } else {

                                    mDatabase.child("users").child("organisers").child(arrayCopy.get(position).getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Organiser organiser = dataSnapshot.getValue(Organiser.class);
                                            ConversationActivity.nameChat = organiser.getCompany();
                                            Intent intent = new Intent(getContext(), ConversationActivity.class);
                                            intent.putExtra("chat", arrayCopy.get(position));
                                            intent.putExtra("class", "fragment");
                                            startActivity(intent);

                                            mDatabase.child("conversation").orderByChild("uuid").equalTo(arrayCopy.get(position).getUuid()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    long size = dataSnapshot.getChildrenCount();
                                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                        if (size > 100) {
                                                            dataSnapshot1.getRef().removeValue();
                                                            size--;
                                                        } else {
                                                            break;
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

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
                });

                listChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Delete conversation?")
                                .setCancelable(true)
                                .setMessage("Are you sure you want to delete this conversation?")
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        mDatabase.child("conversation").orderByChild("uuid").equalTo(arrayCopy.get(position).getUuid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                    dataSnapshot1.getRef().removeValue();
                                                }

                                            }


                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                        chatAdapter.remove(arrayCopy.get(position));
                                        arrayCopy.remove(arrayCopy.get(position));
                                        chatAdapter.notifyDataSetChanged();

                                        if (chatAdapter.isEmpty()) {
                                            noChatTextView.setVisibility(View.VISIBLE);
                                            listChat.setVisibility(View.GONE);
                                        }

                                    }
                                });


                        alert.show();

                        return true;
                    }
                });

                if (arrayCopy.isEmpty()) {
                    noChatTextView.setVisibility(View.VISIBLE);
                    listChat.setVisibility(View.GONE);
                } else {
                    noChatTextView.setVisibility(View.GONE);
                    listChat.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return v;
    }
}