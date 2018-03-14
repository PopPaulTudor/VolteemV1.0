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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.EventVolunteersAdapter;
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.RegisteredUser;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventAcceptedUsersFragment extends Fragment implements ActionListener.VolunteersRemovedListener{

    public static EventVolunteersAdapter adapter;
    private ArrayList<String> mAcceptedUsers = new ArrayList<>();
    private ArrayList<Volunteer> mVolunteers = new ArrayList<>();
    private Event currentEvent;
    private RecyclerView mAcceptedUsersList;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private ProgressBar progressBar;
    private TextView noVolunteersText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organiser_single_event_accepted_users, container, false);

        currentEvent = (Event) getArguments().getSerializable("currentEvent");
        mAcceptedUsers = currentEvent.getAccepted_volunteers();
        progressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        noVolunteersText = (TextView) view.findViewById(R.id.no_volunteers);
        mAcceptedUsersList = (RecyclerView) view.findViewById(R.id.RecViewAccUsers);
        mAcceptedUsersList.setHasFixedSize(true);

        return view;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        currentEvent = currentEvent == null ? (Event) getArguments().getSerializable("currentEvent") : currentEvent;
        Log.d("EventID", currentEvent.getEventID());

        if (isVisibleToUser) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();
            database.child("events").child(currentEvent.getEventID()).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("SingleEventFragment", "Refreshing accepted users.");
                    mAcceptedUsers = new ArrayList<>();

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
        progressBar.setVisibility(View.VISIBLE);
        if (!mAcceptedUsers.isEmpty()) {
            noVolunteersText.setVisibility(View.GONE);
            for (final String volunteerID : mAcceptedUsers) {
                mVolunteers = new ArrayList<>();
                mDatabase.child("users").child("volunteers").child(volunteerID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Volunteer volunteer;
                        volunteer = dataSnapshot.getValue(Volunteer.class);
                        mVolunteers.add(volunteer);
                        if (TextUtils.equals(mAcceptedUsers.get(mAcceptedUsers.size() - 1), volunteerID)) {

                            progressBar.setVisibility(View.GONE);
                            adapter = new EventVolunteersAdapter(mVolunteers, mAcceptedUsers, "accept", currentEvent, getContext(), getActivity(), OrganiserSingleEventAcceptedUsersFragment.this);
                            mAcceptedUsersList.setAdapter(adapter);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            mAcceptedUsersList.setLayoutManager(linearLayoutManager);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("OrgSingleAccF", databaseError.getMessage());
                    }
                });
            }
        } else {
            progressBar.setVisibility(View.GONE);
            noVolunteersText.setVisibility(View.VISIBLE);
            mVolunteers = new ArrayList<>();
            adapter = new EventVolunteersAdapter(mVolunteers, mAcceptedUsers, "accept", currentEvent, getContext(), getActivity(), OrganiserSingleEventAcceptedUsersFragment.this);
            mAcceptedUsersList.setAdapter(adapter);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
            mAcceptedUsersList.setLayoutManager(linearLayoutManager);
        }
    }

    @Override
    public void onAllVolunteersRemoved() {
        noVolunteersText.setVisibility(View.VISIBLE);
    }
}
