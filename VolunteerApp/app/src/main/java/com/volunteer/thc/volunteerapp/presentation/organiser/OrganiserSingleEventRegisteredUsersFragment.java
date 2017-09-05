package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventRegisteredUsersFragment extends Fragment {

    private ArrayList<String> mRegisteredUsers = new ArrayList<>();
    private ArrayList<Volunteer> mVolunteers = new ArrayList<>();
    private RecyclerView mRegisteredUsersRecView;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private Event currentEvent;
    public static EventVolunteersAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiser_single_event_registered_users, container, false);

        currentEvent = (Event) getArguments().getSerializable("currentEvent");
        mRegisteredUsers = currentEvent.getRegistered_volunteers();
        mRegisteredUsersRecView = (RecyclerView) view.findViewById(R.id.RecViewRegUsers);
        mRegisteredUsersRecView.setHasFixedSize(true);


        for (final String volunteerID : mRegisteredUsers) {
            mVolunteers = new ArrayList<>();
            mDatabase.child("users").child("volunteers").child(volunteerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Volunteer volunteer;
                    volunteer = dataSnapshot.getValue(Volunteer.class);
                    mVolunteers.add(volunteer);
                    if (TextUtils.equals(mRegisteredUsers.get(mRegisteredUsers.size() - 1), volunteerID)) {
                        quicksort(0, mVolunteers.size() - 1);
                        adapter = new EventVolunteersAdapter(mVolunteers, mRegisteredUsers, "reg", currentEvent,getContext(),OrganiserSingleEventRegisteredUsersFragment.this);
                        mRegisteredUsersRecView.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        mRegisteredUsersRecView.setLayoutManager(linearLayoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        return view;
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
}
