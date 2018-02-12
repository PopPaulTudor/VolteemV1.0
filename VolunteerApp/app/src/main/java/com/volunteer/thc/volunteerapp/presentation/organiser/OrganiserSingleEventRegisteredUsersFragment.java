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
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventRegisteredUsersFragment extends Fragment implements ActionListener.VolunteersRemovedListener{

    public static EventVolunteersAdapter adapter;
    private ArrayList<String> mRegisteredUsers;
    private ArrayList<Volunteer> mVolunteers = new ArrayList<>();
    private RecyclerView mRegisteredUsersRecView;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private Event currentEvent;
    private ProgressBar progressBar;
    private TextView noVolunteersText;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiser_single_event_registered_users, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        noVolunteersText = (TextView) view.findViewById(R.id.no_volunteers);
        currentEvent = (Event) getArguments().getSerializable("currentEvent");
        mRegisteredUsersRecView = (RecyclerView) view.findViewById(R.id.RecViewRegUsers);
        mRegisteredUsersRecView.setHasFixedSize(true);

        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("events/" + currentEvent.getEventID() + "/users").orderByChild("status").equalTo("pending").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRegisteredUsers = new ArrayList<>();
                if(dataSnapshot.exists()) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        mRegisteredUsers.add(dataSnapshot1.child("id").getValue().toString());
                    }
                    getVolunteersData();
                } else {
                    noVolunteersText.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    adapter = new EventVolunteersAdapter(mVolunteers, mRegisteredUsers, "reg", currentEvent, getContext(), OrganiserSingleEventRegisteredUsersFragment.this, getActivity(), OrganiserSingleEventRegisteredUsersFragment.this);
                    mRegisteredUsersRecView.setAdapter(adapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    mRegisteredUsersRecView.setLayoutManager(linearLayoutManager);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OrgSingleRegF", databaseError.getMessage());
            }
        });

        return view;
    }

    private void getVolunteersData() {
        for (final String volunteerID : mRegisteredUsers) {
            mVolunteers = new ArrayList<>();
            mDatabase.child("users").child("volunteers").child(volunteerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        Volunteer volunteer;
                        volunteer = dataSnapshot.getValue(Volunteer.class);
                        mVolunteers.add(volunteer);
                        if (TextUtils.equals(mRegisteredUsers.get(mRegisteredUsers.size() - 1), volunteerID)) {
                            progressBar.setVisibility(View.GONE);
                            quicksort(0, mVolunteers.size() - 1);
                            adapter = new EventVolunteersAdapter(mVolunteers, mRegisteredUsers, "reg", currentEvent, getContext(), OrganiserSingleEventRegisteredUsersFragment.this, getActivity(), OrganiserSingleEventRegisteredUsersFragment.this);
                            mRegisteredUsersRecView.setAdapter(adapter);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                            mRegisteredUsersRecView.setLayoutManager(linearLayoutManager);
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("OrgSingleRegFGetData", databaseError.getMessage());
                }
            });
        }
    }

    private void quicksort(int inf, int sup) {
        int i = inf, j = sup, pivot = mVolunteers.get((i + j) / 2).getExperience();
        Volunteer auxVolunteer;
        String auxString;
        while (i <= j) {
            while (i < sup && mVolunteers.get(i).getExperience() > pivot) ++i;
            while (j > inf && mVolunteers.get(j).getExperience() < pivot) --j;
            if (i <= j) {

                auxVolunteer = mVolunteers.get(i);
                mVolunteers.set(i, mVolunteers.get(j));
                mVolunteers.set(j, auxVolunteer);

                auxString = mRegisteredUsers.get(i);
                mRegisteredUsers.set(i, mRegisteredUsers.get(j));
                mRegisteredUsers.set(j, auxString);

                ++i;
                --j;
            }
        }
        if (inf < j) quicksort(inf, j);
        if (i < sup) quicksort(i, sup);
    }

    @Override
    public void onAllVolunteersRemoved() {
        noVolunteersText.setVisibility(View.VISIBLE);
    }
}
