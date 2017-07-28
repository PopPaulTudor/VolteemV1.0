package com.volunteer.thc.volunteerapp.presentation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.OrgEventsAdaptor;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.Organiser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristi on 6/17/2017.
 */

public class OrganiserEventsFragment extends Fragment {

    private FloatingActionButton mAddEvent;
    private FragmentTransaction mFragmentTransaction;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<Event> mEventsList = new ArrayList<>();
    private ArrayList<String> mEventIDs = new ArrayList<>();
    private Organiser organiser = new Organiser();
    private ValueEventListener mSingleEventListener;
    private int indexOfEvent = 1;
    private RecyclerView recyclerView;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserevents, container, false);

        mAddEvent = (FloatingActionButton) view.findViewById(R.id.add_event);

        recyclerView = (RecyclerView) view.findViewById(R.id.RecViewOrgEvents);
        recyclerView.setHasFixedSize(true);
        mAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateEventFragment();
            }
        });

        mProgressDialog = ProgressDialog.show(getActivity(), "Getting events...", "", true);

        if(isNetworkAvailable()) {

            mDatabase.child("users").child("organisers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    organiser = dataSnapshot.getValue(Organiser.class);
                    mEventIDs = organiser.getEvents();
                    if (mEventIDs == null) {

                        mProgressDialog.dismiss();
                        Snackbar snackbar = Snackbar.make(getView(), "You don't have any events. How about creating one now?", Snackbar.LENGTH_LONG).setAction("Action", null);
                        snackbar.setAction("Add", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openCreateEventFragment();
                            }
                        });
                        snackbar.show();

                    } else {
                        getSingleEvent(0);
                        Activity activity = getActivity();
                        if(activity != null) {
                            SharedPreferences prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                            prefs.edit().putInt("lastID", mEventIDs.size()).apply();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mSingleEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    String name, location, date, type, description, deadline, created_by, eventID;
                    int size;
                    ArrayList<String> registeredUsers = new ArrayList<>();
                    ArrayList<String> acceptedUsers = new ArrayList<>();

                    created_by = dataSnapshot.child("created_by").getValue().toString();
                    eventID = dataSnapshot.child("eventID").getValue().toString();
                    name = dataSnapshot.child("name").getValue().toString();
                    location = dataSnapshot.child("location").getValue().toString();
                    size = Integer.parseInt(dataSnapshot.child("size").getValue().toString());
                    date = dataSnapshot.child("date").getValue().toString();
                    type = dataSnapshot.child("type").getValue().toString();
                    description = dataSnapshot.child("description").getValue().toString();
                    deadline = dataSnapshot.child("deadline").getValue().toString();

                    for (DataSnapshot registered_users : dataSnapshot.child("registered_users").getChildren()) {

                        registeredUsers.add(registered_users.child("user").getValue().toString());
                    }

                    for (DataSnapshot registered_users : dataSnapshot.child("accepted_users").getChildren()) {

                        acceptedUsers.add(registered_users.child("user").getValue().toString());
                    }

                    mEventsList.add(new Event(created_by, name, location, date, type, eventID, description, deadline, size, registeredUsers, acceptedUsers));

                    if (indexOfEvent < mEventIDs.size()) {
                        getSingleEvent(indexOfEvent);
                        ++indexOfEvent;
                    } else {

                        mProgressDialog.dismiss();
                        OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList, getContext());
                        recyclerView.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Log.e("Read", "error");
                }
            };

        } else {

            mProgressDialog.dismiss();
            Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_LONG).show();
        }
        return view;
    }

    public void getSingleEvent(int index) {

        mDatabase.child("events").child(mEventIDs.get(index)).addListenerForSingleValueEvent(mSingleEventListener);
    }

    public void openCreateEventFragment(){

        mFragmentTransaction = getFragmentManager().beginTransaction();
        mFragmentTransaction.replace(R.id.main_container, new CreateEventFragment());
        mFragmentTransaction.commit();
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
