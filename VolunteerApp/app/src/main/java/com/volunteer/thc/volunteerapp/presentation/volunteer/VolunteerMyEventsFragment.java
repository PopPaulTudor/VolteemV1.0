package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private TextView noEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteer_my_events, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.RecViewVolEvents);
        recyclerView.setHasFixedSize(true);
        mProgressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        noEvents = (TextView) view.findViewById(R.id.no_events_text);
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
                    final Event currentEvent = eventSnapshot.getValue(Event.class);
                    if (isUserRegisteredForEvent(currentEvent.getEventID())) {
                        mEventsList.add(currentEvent);
                    }
                }
                if (mEventsList.isEmpty()) {
                    noEvents.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
                OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList, getContext(),getResources());
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
