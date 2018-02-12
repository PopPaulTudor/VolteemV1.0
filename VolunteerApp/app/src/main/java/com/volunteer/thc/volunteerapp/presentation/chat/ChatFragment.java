package com.volunteer.thc.volunteerapp.presentation.chat;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
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
import com.volunteer.thc.volunteerapp.adapter.ChatAdapter;
import com.volunteer.thc.volunteerapp.model.ChatGroup;
import com.volunteer.thc.volunteerapp.model.ChatSingle;
import com.volunteer.thc.volunteerapp.model.Message;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by poppa on 25.08.2017.
 */
public class ChatFragment extends Fragment {

    final ArrayList<Message> array = new ArrayList<>();
    final ArrayList<Message> arrayWork = new ArrayList<>();
    View rootLayout;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ListView listChat;
    private ChatAdapter chatAdapter;
    private TextView noChatText;
    private ImageView noChatImage;
    private String type = "single";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        listChat = (ListView) v.findViewById(R.id.list_chat);

        chatAdapter = new ChatAdapter(getContext(), array);
        listChat.setAdapter(chatAdapter);
        noChatImage = (ImageView) v.findViewById(R.id.no_chat_image);
        rootLayout = v.findViewById(R.id.root_layout);
        noChatText = (TextView) v.findViewById(R.id.no_chat_text);
        final FloatingActionButton floatingActionButton = (FloatingActionButton) v.findViewById(R.id.change_list_chat);
        populateList(type);


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                if (type.equals("single")) {
                    type = "group";
                    floatingActionButton.setImageResource(R.drawable.ic_person_black_24dp);
                    floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(19, 122, 106)));

                } else {
                    type = "single";
                    floatingActionButton.setImageResource(R.drawable.ic_group_black_24dp);
                    floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(19, 122, 106)));
                }
                clearLists();
                presentActivity(v);


            }
        });
        return v;

    }

    private void populateList(final String type) {

        mDatabase.child("conversation").child(type).orderByChild("receivedBy").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                noChatImage.setVisibility(View.GONE);
                noChatText.setVisibility(View.GONE);

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Message chatData;
                    if (type.equals("single")) chatData = data.getValue(ChatSingle.class);
                    else chatData = data.getValue(ChatGroup.class);
                    boolean change = false;
                    for (Message message : arrayWork) {
                        if (message.getSentBy().equals(chatData.getSentBy())) {
                            change = true;
                            break;
                        }
                    }
                    if (!change) {
                        arrayWork.add(chatData);
                        chatAdapter.add(chatData);
                        chatAdapter.notifyDataSetChanged();

                    }
                }

                listChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        mDatabase.child("users").child("volunteers").child(arrayWork.get(position).getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Volunteer volunteer = dataSnapshot.getValue(Volunteer.class);
                                    ConversationActivity.nameChat = volunteer.getFirstname() + " " + volunteer.getLastname();
                                    Intent intent = new Intent(getContext(), ConversationActivity.class);
                                    intent.putExtra("chat", arrayWork.get(position));
                                    intent.putExtra("class", "fragment");
                                    intent.putExtra("type", type);
                                    startActivity(intent);

                                } else {
                                    mDatabase.child("users").child("organisers").child(arrayWork.get(position).getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Organiser organiser = dataSnapshot.getValue(Organiser.class);
                                            ConversationActivity.nameChat = organiser.getCompany();
                                            Intent intent = new Intent(getContext(), ConversationActivity.class);
                                            intent.putExtra("chat", arrayWork.get(position));
                                            intent.putExtra("class", "fragment");
                                            intent.putExtra("type", type);
                                            startActivity(intent);

                                            mDatabase.child("conversation").child(type).orderByChild("uuid").equalTo(arrayWork.get(position).getUuid()).addValueEventListener(new ValueEventListener() {
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

                        // TODO: 07.12.2017 don't allow anyone to delete
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
                                        mDatabase.child("conversation").child(type).orderByChild("uuid").equalTo(arrayWork.get(position).getUuid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                        chatAdapter.remove(arrayWork.get(position));
                                        arrayWork.remove(arrayWork.get(position));
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

                if (arrayWork.isEmpty()) {
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

    }

    public void presentActivity(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rootLayout.setVisibility(View.INVISIBLE);
            final int revealX = (int) (view.getX() + view.getWidth() / 2);
            final int revealY = (int) (view.getY() + view.getHeight() / 2);


            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();

            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }

    }


    void revealActivity(int x, int y) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            float finalRadius = (float) (Math.max(width, height) * 1.1);

            Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
            circularReveal.setDuration(400);
            circularReveal.setInterpolator(new AccelerateInterpolator());

            rootLayout.setVisibility(View.VISIBLE);
            circularReveal.start();
        }

    }


    void clearLists() {

        chatAdapter.clear();
        arrayWork.clear();
        array.clear();
        populateList(type);
        chatAdapter.notifyDataSetChanged();
    }


}
