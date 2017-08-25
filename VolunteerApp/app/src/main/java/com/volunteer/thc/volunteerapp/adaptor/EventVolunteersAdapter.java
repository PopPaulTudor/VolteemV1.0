package com.volunteer.thc.volunteerapp.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Volunteer;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserEventsFragment;

import java.util.ArrayList;

/**
 * Created by poppa on 28.07.2017.
 */

public class EventVolunteersAdapter extends RecyclerView.Adapter<EventVolunteersAdapter.EventViewHolder> {

    private ArrayList<Volunteer> listVolunteer;
    private ArrayList<String> volunteerIDs;
    private String classParent, eventID;
    private int mExpandedPosition = -1;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ViewGroup parent;
    private Context context;

    public EventVolunteersAdapter(ArrayList<Volunteer> list, ArrayList<String> volunteerIDs, String classParent, String eventID, Context context) {
        listVolunteer = list;
        this.classParent = classParent;
        this.volunteerIDs = volunteerIDs;
        this.eventID = eventID;
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
                mDatabase.child("events").child(eventID).child("users").child(volunteerIDs.get(position)).child("status").setValue("accepted");
                Toast.makeText(parent.getContext(), "Accepted volunteer!", Toast.LENGTH_LONG).show();
                listVolunteer.remove(position);
                volunteerIDs.remove(position);
                notifyDataSetChanged();
                OrganiserEventsFragment.hasActionHappened = true;
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
        Button acceptUser;

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
        }
    }
}
