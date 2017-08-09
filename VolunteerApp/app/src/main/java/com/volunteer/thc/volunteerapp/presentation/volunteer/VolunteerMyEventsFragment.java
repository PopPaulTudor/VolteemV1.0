package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
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
import com.volunteer.thc.volunteerapp.adaptor.OrgEventsAdaptor;
import com.volunteer.thc.volunteerapp.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Cristi on 7/15/2017.
 */

public class VolunteerMyEventsFragment extends Fragment {

    private List<Event> mEventsList = new ArrayList<>();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;
    private ValueEventListener mRetrieveEvents;
    private ArrayList<String> mUserEvents = new ArrayList<>();
    private Calendar date = Calendar.getInstance();
    private RatingBar ratingBar;
    private View alertView;
    private TextView noStarsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteer_my_events, container, false);
        alertView = inflater.inflate(R.layout.volunteer_alert_dialog, null);
        ratingBar = (RatingBar) alertView.findViewById(R.id.ratingBar);
        noStarsText = (TextView) alertView.findViewById(R.id.noStarsText);
        recyclerView = (RecyclerView) view.findViewById(R.id.RecViewVolEvents);
        recyclerView.setHasFixedSize(true);
        mProgressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserEvents = new ArrayList<>();
                for (DataSnapshot usersSnapshot : dataSnapshot.child("events").getChildren()) {
                    mUserEvents.add(usersSnapshot.getValue().toString());
                }
                mDatabase.child("events").addListenerForSingleValueEvent(mRetrieveEvents);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRetrieveEvents = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventsList = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    Event currentEvent = eventSnapshot.getValue(Event.class);
                    if (isUserRegisteredForEvent(currentEvent.getEventID())) {
                        if (currentEvent.getFinishDate() < date.getTimeInMillis()) {

                            final AlertDialog alert = new AlertDialog.Builder(getActivity())
                                    .setView(alertView)
                                    .setTitle("Event finished")
                                    .setMessage("One of the events you volunteered for, " + currentEvent.getName() + ", has finished. " +
                                            "Please give the event organiser a rating. ")
                                    .setCancelable(false)
                                    .setPositiveButton("DONE", null)
                                    .create();

                            alert.show();
                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    int starsCount = (int) ratingBar.getRating();
                                    Log.w("rating", starsCount + "");
                                    if (starsCount > 0) {
                                        alert.dismiss();
                                        Toast.makeText(getActivity(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                                        //TODO: update organiser's rating
                                        //Problem: what if 2 or more users want to change the rating at the same time?
                                    } else {
                                        noStarsText.setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                        } else {
                            mEventsList.add(currentEvent);
                        }
                    }
                }
                mProgressBar.setVisibility(View.GONE);
                OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList, getContext());
                recyclerView.setAdapter(adapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(linearLayoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private boolean isUserRegisteredForEvent(String eventID) {

        for (String event : mUserEvents) {
            if (TextUtils.equals(eventID, event)) {
                return true;
            }
        }
        return false;
    }
}
