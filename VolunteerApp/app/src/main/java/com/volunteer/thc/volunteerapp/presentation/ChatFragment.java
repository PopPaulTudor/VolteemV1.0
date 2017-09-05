package com.volunteer.thc.volunteerapp.presentation;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
    private TextView noChatText;
    private ImageView noChatImage;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        listChat = (ListView) v.findViewById(R.id.list_chat);
        final ArrayList<Chat> array = new ArrayList<>();
        final ArrayList<Chat> arrayCopy = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), array);
        listChat.setAdapter(chatAdapter);

        noChatImage = (ImageView) v.findViewById(R.id.no_chat_image);
        noChatText = (TextView) v.findViewById(R.id.no_chat_text);


        mDatabase.child("conversation").orderByChild("receivedBy").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                noChatImage.setVisibility(View.GONE);
                noChatText.setVisibility(View.GONE);

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Chat chatData = data.getValue(Chat.class);
                    boolean change = false;
                    for (Chat chat : array) {
                        if (chat.getReceivedBy().equals(chatData.getReceivedBy())) {
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
                        intent.putExtra("class", "fragment");
                        startActivity(intent);
                    }
                });

                listChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Delete conversation?")
                                .setCancelable(true)
                                .setMessage("Are you sure you want to delete this conversation?")
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
                                            noChatImage.setVisibility(View.VISIBLE);
                                            noChatText.setVisibility(View.VISIBLE);
                                            listChat.setVisibility(View.GONE);
                                        }

                                    }
                                });


                        alert.show();

                        return true;
                    }
                });

                if (arrayCopy.isEmpty()) {
                    noChatImage.setVisibility(View.VISIBLE);
                    noChatText.setVisibility(View.VISIBLE);
                    listChat.setVisibility(View.GONE);
                } else {
                    noChatImage.setVisibility(View.GONE);
                    noChatText.setVisibility(View.GONE);
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
