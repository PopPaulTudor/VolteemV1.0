package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.EventVolunteersAdapter;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.RegisteredUser;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventAcceptedUsersFragment extends Fragment {

    private ArrayList<String> mAcceptedUsers = new ArrayList<>();
    private ArrayList<Volunteer> mVolunteers = new ArrayList<>();
    private String eventID;
    private RecyclerView mAcceptedUsersList;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organiser_single_event_accepted_users, container, false);
        mAcceptedUsersList = (RecyclerView) view.findViewById(R.id.RecViewAccUsers);
        mAcceptedUsersList.setHasFixedSize(true);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        eventID = eventID == null ? getArguments().getString("eventID") : eventID;
        Log.d("EventID", eventID);

        if (isVisibleToUser) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child("events").child(eventID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("SingleEventFragment", "Refreshing accepted users.");
                    mAcceptedUsers = new ArrayList<String>();

                    if (dataSnapshot!= null) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            RegisteredUser registeredUser = data.getValue(RegisteredUser.class);
                            if(TextUtils.equals(registeredUser.getStatus(), "accepted")) {
                                mAcceptedUsers.add(registeredUser.getId());
                            }
                        }
                    }

                    refreshList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("SingleEventFragment", databaseError.getMessage());
                }
            });
        }
    }

    public void refreshList() {
        if (mAcceptedUsers != null) {
            for (final String volunteerID : mAcceptedUsers) {
                mVolunteers = new ArrayList<>();
                mDatabase.child("users").child("volunteers").child(volunteerID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Volunteer volunteer;
                        volunteer = dataSnapshot.getValue(Volunteer.class);
                        mVolunteers.add(volunteer);
                        if (TextUtils.equals(mAcceptedUsers.get(mAcceptedUsers.size() - 1), volunteerID)) {

                            EventVolunteersAdapter adapter = new EventVolunteersAdapter(mVolunteers, mAcceptedUsers, "accept", eventID, getContext());
                            mAcceptedUsersList.setAdapter(adapter);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            mAcceptedUsersList.setLayoutManager(linearLayoutManager);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
