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
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.OrgEventsAdaptor;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.OrganiserRating;

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
    private TextView noEvents;
    private Calendar date = Calendar.getInstance();

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
        mDatabase.child("events").orderByChild("users/" + user.getUid() + "/flag").equalTo("valid").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventsList = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    final Event currentEvent = eventSnapshot.getValue(Event.class);
                    if (currentEvent.getFinishDate() > date.getTimeInMillis()) {
                        mEventsList.add(currentEvent);
                    } else {
                        if (isFragmentActive()) {
                            mDatabase.child("events/" + currentEvent.getEventID() + "/users/" + user.getUid() + "/flag").setValue("done");
                            final boolean isUserAccepted = TextUtils.equals(eventSnapshot.child("users").child(user.getUid())
                                    .child("status").getValue().toString(), "accepted");
                            if (isUserAccepted) {
                                mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long experience = dataSnapshot.child("experience").getValue(Long.class);
                                        mDatabase.child("users").child("volunteers").child(user.getUid()).child("experience")
                                                .setValue(experience + (currentEvent.getSize() * 5));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                View alertView = getActivity().getLayoutInflater().inflate(R.layout.volunteer_alert_dialog, null);
                                final AlertDialog alert = new AlertDialog.Builder(getActivity())
                                        .setView(alertView)
                                        .setTitle("Event finished")
                                        .setMessage("One of the events you volunteered for, " + currentEvent.getName() + ", has finished. " +
                                                "Please give the event organiser a rating. ")
                                        .setCancelable(false)
                                        .setPositiveButton("DONE", null)
                                        .create();

                                final RatingBar ratingBar = (RatingBar) alertView.findViewById(R.id.ratingBar);
                                final TextView noStarsText = (TextView) alertView.findViewById(R.id.noStarsText);

                                alert.show();
                                alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final int starsCount = (int) ratingBar.getRating();
                                        Log.w("rating", starsCount + "");
                                        if (starsCount > 0) {
                                            alert.dismiss();
                                            mDatabase.child("users").child("organisers").child(currentEvent.getCreated_by())
                                                    .child("org_rating").runTransaction(new Transaction.Handler() {
                                                @Override
                                                public Transaction.Result doTransaction(MutableData mutableData) {
                                                    OrganiserRating organiserRating = mutableData.getValue(OrganiserRating.class);
                                                    if (organiserRating == null) {
                                                        return Transaction.success(mutableData);
                                                    }

                                                    organiserRating.calculateNewRating(starsCount);
                                                    mutableData.setValue(organiserRating);
                                                    return Transaction.success(mutableData);
                                                }

                                                @Override
                                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                    Log.e("Transaction", "onComplete:" + databaseError);
                                                }
                                            });
                                            Toast.makeText(getActivity(), "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            noStarsText.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                if (mEventsList.isEmpty()) {
                    noEvents.setVisibility(View.VISIBLE);
                }
                if(isFragmentActive()) {
                    mProgressBar.setVisibility(View.GONE);
                    OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList, getContext(), getResources());
                    recyclerView.setAdapter(adapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(linearLayoutManager);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isFragmentActive() {
        return isAdded() && !isDetached() && !isRemoving();
    }
}
