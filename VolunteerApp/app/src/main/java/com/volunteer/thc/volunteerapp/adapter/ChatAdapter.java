package com.volunteer.thc.volunteerapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.ChatGroup;
import com.volunteer.thc.volunteerapp.model.ChatSingle;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.Message;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.chat.ConversationActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by poppa on 25.08.2017.
 */

public class ChatAdapter extends ArrayAdapter<Message> {

    public ArrayList<Message> data = new ArrayList<>();
    private Context context;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();


    public ChatAdapter(Context context, ArrayList<Message> objects) {
        super(context, R.layout.chat_item, objects);
        this.context = context;
        this.data = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Message message = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.chat_item, parent, false);
            viewHolder.textElement = (TextView) convertView.findViewById(R.id.chat_text_element);
            viewHolder.imageElement = (CircleImageView) convertView.findViewById(R.id.chat_icon_element);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (message instanceof ChatSingle) {

            mDatabase.child("users").child("volunteers").child(message.getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Volunteer volunteer = dataSnapshot.getValue(Volunteer.class);
                        ConversationActivity.nameChat = null;
                        viewHolder.textElement.setText(volunteer.getFirstname() + " " + volunteer.getLastname());
                        ConversationActivity.nameChat = volunteer.getFirstname() + " " + volunteer.getLastname();

                    } else {
                        mDatabase.child("users").child("organisers").child(message.getSentBy()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Organiser organiser = dataSnapshot.getValue(Organiser.class);
                                viewHolder.textElement.setText(organiser.getCompany());


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

            storageRef.child("Photos").child("User").child(message.getSentBy()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri).fit().centerCrop().into(viewHolder.imageElement);
                }
            });
        } else {
            ChatGroup chatGroup = (ChatGroup) message;
            mDatabase.child("events/" + chatGroup.getUuidEvent()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = dataSnapshot.getValue(Event.class);
                    viewHolder.textElement.setText(event.getName());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            storageRef.child("Photos").child("Event").child(chatGroup.getUuidEvent()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(context).load(uri).fit().centerCrop().into(viewHolder.imageElement);

                }
            });
        }

        return convertView;
    }


    private static class ViewHolder {
        TextView textElement;
        CircleImageView imageElement;
    }

}
