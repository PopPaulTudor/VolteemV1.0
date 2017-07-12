package com.volunteer.thc.volunteerapp.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
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

        mDatabase.child("users").child("organisers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                organiser = dataSnapshot.getValue(Organiser.class);
                mEventIDs = organiser.getEvents();
                if(mEventIDs == null) {

                    Snackbar snackbar= Snackbar.make(getView(), "You don't have any events. How about creating one now?", Snackbar.LENGTH_LONG).setAction("Action", null);
                    snackbar.setAction("Add", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openCreateEventFragment();
                        }
                    });
                    snackbar.show();

                } else {
                    getSingleEvent(0);
                    SharedPreferences prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    prefs.edit().putInt("lastID", mEventIDs.size()).apply();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSingleEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                mEventsList.add(dataSnapshot.getValue(Event.class));
                if(indexOfEvent < mEventIDs.size()) {
                    getSingleEvent(indexOfEvent);
                    ++indexOfEvent;
                } else {

                    mProgressDialog.dismiss();
                    OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList);
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

}
