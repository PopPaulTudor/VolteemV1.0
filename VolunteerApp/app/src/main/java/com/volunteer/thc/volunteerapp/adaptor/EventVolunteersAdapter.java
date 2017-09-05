package com.volunteer.thc.volunteerapp.adaptor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Chat;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.ConversationActivity;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserEventsFragment;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserSingleEventRegisteredUsersFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

/**
 * Created by poppa on 28.07.2017.
 */

public class EventVolunteersAdapter extends RecyclerView.Adapter<EventVolunteersAdapter.EventViewHolder> {

    private static ArrayList<Volunteer> listVolunteer;
    private static ArrayList<String> volunteerIDs;
    private String classParent;
    private static Event event;
    private int mExpandedPosition = -1;
    private static DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ViewGroup parent;
    private Context context;
    private Calendar date = Calendar.getInstance();
    private static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private OrganiserSingleEventRegisteredUsersFragment fragment;

    public EventVolunteersAdapter(ArrayList<Volunteer> list, ArrayList<String> volunteerIDs, String classParent, Event event, Context context, OrganiserSingleEventRegisteredUsersFragment fragment) {
        listVolunteer = list;
        this.classParent = classParent;
        this.volunteerIDs = volunteerIDs;
        this.event = event;
        this.context = context;
        this.fragment = fragment;
    }

    public EventVolunteersAdapter(ArrayList<Volunteer> list, ArrayList<String> volunteerIDs, String classParent, Event event, Context context) {
        listVolunteer = list;
        this.classParent = classParent;
        this.volunteerIDs = volunteerIDs;
        this.event = event;
        this.context = context;
    }


    @Override
    public EventVolunteersAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.volunteer_element, parent, false);

        this.parent = parent;
        return new EventVolunteersAdapter.EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final EventVolunteersAdapter.EventViewHolder holder, final int position) {

        holder.nameVolunteer.setText(listVolunteer.get(position).getFirstname() + " " + listVolunteer.get(position).getLastname());
        holder.cityVolunteer.setText("City: " + listVolunteer.get(position).getCity());
        holder.ageVolunteer.setText("Age: " + listVolunteer.get(position).getAge());
        holder.emailVolunteer.setText("Email: " + listVolunteer.get(position).getEmail());
        if (classParent.contains("accept")) {
            holder.phoneVolunteer.setText("Experience: " + listVolunteer.get(position).getExperience());
            holder.acceptUser.setVisibility(View.GONE);
            holder.expPhoneVolunteer.setText(listVolunteer.get(position).getPhone());
        } else {
            holder.phoneVolunteer.setText("Phone: " + listVolunteer.get(position).getPhone());
            holder.acceptUser.setVisibility(View.VISIBLE);
            holder.expPhoneVolunteer.setText(listVolunteer.get(position).getExperience() + "");
        }

        final boolean isExpanded = position == mExpandedPosition;
        holder.expandableItem.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.item.setActivated(isExpanded);
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedPosition = isExpanded ? -1 : position;
                notifyDataSetChanged();
            }
        });


        holder.acceptUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eventID = mDatabase.child("news").push().getKey();
                mDatabase.child("news").child(eventID).setValue(new NewsMessage(date.getTimeInMillis(), eventID, event.getEventID(), event.getCreated_by(), volunteerIDs.get(position),
                        "You have been accepted at " + event.getName() + "!", NewsMessage.ACCEPT, false, false));

                mDatabase.child("events").child(event.getEventID()).child("users").child(volunteerIDs.get(position)).child("status").setValue("accepted");
                Toast.makeText(parent.getContext(), "Accepted volunteer!", Toast.LENGTH_LONG).show();

                Chat chat = new Chat(event.getCreated_by(), volunteerIDs.get(position), "You have been accepted to " + event.getName(), UUID.randomUUID().toString(), Calendar.getInstance().getTimeInMillis(), false);
                mDatabase.child("conversation").push().setValue(chat);

                listVolunteer.remove(position);
                volunteerIDs.remove(position);
                notifyDataSetChanged();


            }
        });

        holder.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(context, ConversationActivity.class);

                mDatabase.child("conversation").orderByChild("receivedBy").equalTo(volunteerIDs.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Chat chat = new Chat(user.getUid(), volunteerIDs.get(position), "", UUID.randomUUID().toString(), 0, false);
                            intent.putExtra("chat", chat);
                        } else {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Chat chat = dataSnapshot1.getValue(Chat.class);
                                intent.putExtra("chat", chat);
                                break;
                            }

                        }
                        ConversationActivity.nameChat = listVolunteer.get(position).getFirstname() + listVolunteer.get(position).getLastname();
                        intent.putExtra("class", "adapter");
                        intent.putExtra("position", position);
                        ConversationActivity.fragment = fragment;
                        context.startActivity(intent);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listVolunteer.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {


        TextView nameVolunteer, expPhoneVolunteer, cityVolunteer, ageVolunteer, phoneVolunteer, emailVolunteer;
        RelativeLayout item;
        LinearLayout expandableItem;
        Button acceptUser, sendMessage;

        EventViewHolder(View itemView) {
            super(itemView);

            item = (RelativeLayout) itemView.findViewById(R.id.item_view);
            nameVolunteer = (TextView) itemView.findViewById(R.id.name_volunteer_element);
            expPhoneVolunteer = (TextView) itemView.findViewById(R.id.exp_phone_volunteer_element);

            expandableItem = (LinearLayout) itemView.findViewById(R.id.expandable_item);
            cityVolunteer = (TextView) itemView.findViewById(R.id.volunteer_city);
            ageVolunteer = (TextView) itemView.findViewById(R.id.volunteer_age);
            phoneVolunteer = (TextView) itemView.findViewById(R.id.volunteer_phone);
            emailVolunteer = (TextView) itemView.findViewById(R.id.volunteer_email);
            acceptUser = (Button) itemView.findViewById(R.id.accept_volunteer);
            sendMessage = (Button) itemView.findViewById(R.id.send_volunteer);
        }
    }

    public static void acceptVolunteer(final int position, Activity activity) {

        String eventID = mDatabase.child("news").push().getKey();
        mDatabase.child("news").child(eventID).setValue(new NewsMessage(Calendar.getInstance().getTimeInMillis(), eventID, event.getCreated_by(), volunteerIDs.get(position),
                "You have been accepted at " + event.getName() + "!", NewsMessage.ACCEPT, false, false));

        mDatabase.child("events").child(event.getEventID()).child("users").child(volunteerIDs.get(position)).child("status").setValue("accepted");
        Toast.makeText(activity, "Accepted volunteer!", Toast.LENGTH_LONG).show();


        mDatabase.child("conversation").orderByChild("receivedBy").equalTo(volunteerIDs.get(position)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String uuid = null;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Chat chatData = data.getValue(Chat.class);
                        uuid = chatData.getUuid();
                        break;

                    }
                } else {
                    uuid = UUID.randomUUID().toString();
                }
                Chat chat = new Chat(event.getCreated_by(), volunteerIDs.get(position), "You have been accepted to " + event.getName(), uuid, Calendar.getInstance().getTimeInMillis(), false);
                mDatabase.child("conversation").push().setValue(chat);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        listVolunteer.remove(position);
        volunteerIDs.remove(position);

        OrganiserEventsFragment.hasActionHappened = true;


    }
}
