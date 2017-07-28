package com.volunteer.thc.volunteerapp.presentation;

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
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventAcceptedUsersFragment extends Fragment {

    private ArrayList<String> mAcceptedUsers = new ArrayList<>();
    private ArrayList<Volunteer> mVolunteers = new ArrayList<>();
    private RecyclerView mAcceptedUsersList;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiser_single_event_accepted_users, container, false);


        mAcceptedUsers = (ArrayList) getArguments().getStringArrayList("accepted_users");
        mAcceptedUsersList = (RecyclerView) view.findViewById(R.id.RecViewAccUsers);
        mAcceptedUsersList.setHasFixedSize(true);


        for (final String volunteerID : mAcceptedUsers) {
            mDatabase.child("users").child("volunteers").child(volunteerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Volunteer volunteer;
                    volunteer = dataSnapshot.getValue(Volunteer.class);
                    mVolunteers.add(volunteer);
                    if (TextUtils.equals(mAcceptedUsers.get(mAcceptedUsers.size() - 1), volunteerID)) {

                        EventVolunteersAdapter adapter = new EventVolunteersAdapter(mVolunteers, "accept");
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


        return view;

    }
}
