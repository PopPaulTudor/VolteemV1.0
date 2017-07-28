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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mEventsList = new ArrayList<>();
        loadEvents();
    }

    private void loadEvents() {
        mProgressDialog = ProgressDialog.show(getActivity(), "Getting events...", "", true);

        if(isNetworkAvailable()) {

            mDatabase.child("events").orderByChild("created_by").equalTo(user.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot event : dataSnapshot.getChildren()) {

                        Event currentEvent = event.getValue(Event.class);
                        ArrayList<String> reg_users = new ArrayList<>();

                        for (DataSnapshot registered_users : event.child("registered_users").getChildren()) {
                            reg_users.add(registered_users.child("user").getValue().toString());
                        }
                        currentEvent.setRegistered_volunteers(reg_users);
                        reg_users = new ArrayList<>();
                        for (DataSnapshot accepted_users : event.child("accepted_users").getChildren()) {
                            reg_users.add(accepted_users.child("user").getValue().toString());
                        }
                        currentEvent.setAccepted_volunteers(reg_users);
                        mEventsList.add(currentEvent);
                    }

                    if(mEventsList.isEmpty()) {
                        
                        Snackbar snackbar = Snackbar.make(getView(), "You don't have any events. How about creating one now?", Snackbar.LENGTH_LONG).setAction("Action", null);
                        snackbar.setAction("Add", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                openCreateEventFragment();
                            }
                        });
                        snackbar.show();

                    } else {

                        mProgressDialog.dismiss();
                        OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList, getContext());
                        recyclerView.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(linearLayoutManager);

                        Activity activity = getActivity();
                        if(activity != null) {
                            SharedPreferences prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                            prefs.edit().putInt("lastID", mEventsList.size()).apply();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Log.e("Read", "error");
                }
            });

        } else {

            mProgressDialog.dismiss();
            Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    public void openCreateEventFragment(){

        mFragmentTransaction = getFragmentManager().beginTransaction();
        mFragmentTransaction.replace(R.id.main_container, new CreateEventFragment());
        mFragmentTransaction.addToBackStack("createEvent");
        mFragmentTransaction.commit();
    }

    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
