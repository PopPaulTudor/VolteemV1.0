package com.volunteer.thc.volunteerapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.util.CalculateUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by poppa on 13.01.2018.
 */

public class VolunteerEventsProfileAdapter extends RecyclerView.Adapter<VolunteerEventsProfileAdapter.EventViewHolder> {

    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
    private ArrayList<Event> events;
    private Context context;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public VolunteerEventsProfileAdapter(ArrayList<Event> events, Context context) {
        this.events = events;
        this.context = context;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_event_element, parent, false);

        return new VolunteerEventsProfileAdapter.EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final EventViewHolder holder, int position) {
        long nrOfDays = TimeUnit.MILLISECONDS.toDays(events.get(position).getFinishDate() - events.get(position).getStartDate());
        long experience= CalculateUtils.calculateVolunteerExperience(events.get(position).getSize(),nrOfDays);

        holder.eventName.setText(events.get(position).getName());
        holder.eventExperience.setText(experience+"");


        storageRef.child("Photos").child("Event").child(events.get(position).getEventId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(context).load(uri).centerCrop().fit().centerCrop().into(holder.eventImage);
            }
        });

        String eventOrgID = events.get(position).getCreatedBy();


        databaseRef.child("users/volunteers/" + user.getUid() + "/feedback/"+eventOrgID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue()==null) {
                    holder.eventFeedback.setText("No feedback");
                } else {

                   holder.eventFeedback.setText(dataSnapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("EvVolAdapterFeedback", databaseError.getMessage());
            }
        });





    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName, eventExperience, eventFeedback;
        ImageView eventImage;
        View view;

        EventViewHolder(View itemView) {
            super(itemView);

            eventName = (TextView) itemView.findViewById(R.id.CardProfileName);
            eventExperience = (TextView) itemView.findViewById(R.id.CardProfileExperience);
            eventImage = (ImageView) itemView.findViewById(R.id.CardProfileImage);
            eventFeedback= (TextView) itemView.findViewById(R.id.CardProfileFeedback);
        }
    }
}
