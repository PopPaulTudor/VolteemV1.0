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
import com.volunteer.thc.volunteerapp.adapter.OrganiserEventsAdapter;
import com.volunteer.thc.volunteerapp.adapter.VolunteerEventsAdapter;
import com.volunteer.thc.volunteerapp.callback.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.OrganiserRating;
import com.volunteer.thc.volunteerapp.util.CalculateUtils;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cristi on 7/15/2017.
 */

public class VolunteerMyEventsFragment extends Fragment implements ActionListener.EventPicturesLoadingListener{

    protected static boolean hasActionHappened = false;
    private List<Event> mEventsList = new ArrayList<>();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ProgressBar mProgressBar;
    private RecyclerView recyclerView;
    private TextView noEvents;
    private Calendar date = Calendar.getInstance();
    private int mLongAnimTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteer_my_events, container, false);
        recyclerView = view.findViewById(R.id.RecViewVolMyEvents);
        recyclerView.setHasFixedSize(true);
        mProgressBar = view.findViewById(R.id.indeterminateBar);
        noEvents = view.findViewById(R.id.no_events_text);

        mLongAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

        loadEvents();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasActionHappened) {
            loadEvents();
            hasActionHappened = false;
        }
    }

    private void loadEvents() {
        mProgressBar.setVisibility(View.VISIBLE);
        mDatabase.child("events").orderByChild("users/" + user.getUid() + "/flag").equalTo(VolteemConstants.VOLUNTEER_EVENT_FLAG_PENDING)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEventsList = new ArrayList<>();
                for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                    final Event currentEvent = eventSnapshot.getValue(Event.class);
                    if (currentEvent.getFinishDate() > date.getTimeInMillis()) {
                        mEventsList.add(currentEvent);
                    } else {
                        if (isFragmentActive()) {
                            mDatabase.child("events/" + currentEvent.getEventID() + "/users/" + user.getUid() + "/flag")
                                    .setValue(VolteemConstants.VOLUNTEER_EVENT_FLAG_DONE);
                            final boolean isUserAccepted = TextUtils.equals(eventSnapshot.child("users").child(user.getUid())
                                    .child("status").getValue().toString(), VolteemConstants.VOLUNTEER_EVENT_STATUS_ACCEPTED);
                            if (isUserAccepted) {
                                mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long experience = dataSnapshot.child("experience").getValue(Long.class);
                                        long nrOfDays = TimeUnit.MILLISECONDS.toDays(currentEvent.getFinishDate() - currentEvent.getStartDate());
                                        mDatabase.child("users").child("volunteers").child(user.getUid()).child("experience")
                                                .setValue(experience + CalculateUtils.calculateVolunteerExperience(currentEvent.getSize(), nrOfDays));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("Volunteers", databaseError.getMessage());
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

                                final RatingBar ratingBar = alertView.findViewById(R.id.ratingBar);
                                final TextView noStarsText = alertView.findViewById(R.id.noStarsText);

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
                    mProgressBar.setVisibility(View.GONE);
                }
                if (isFragmentActive()) {
                    Collections.sort(mEventsList, new Comparator<Event>() {
                        @Override
                        public int compare(Event event, Event t1) {
                            if (event.getStartDate() < t1.getStartDate())
                                return -1;
                            if (event.getStartDate() > t1.getStartDate())
                                return 1;
                            return 0;
                        }
                    });
                    VolunteerEventsAdapter adapter = new VolunteerEventsAdapter(mEventsList,
                            getContext(),
                            getResources(), OrganiserEventsAdapter.MY_EVENTS,
                            VolunteerMyEventsFragment
                            .this, 2);
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

    @Override
    public void onPicturesLoaded() {
        recyclerView.setAlpha(0f);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.animate()
                .alpha(1f)
                .setDuration(mLongAnimTime)
                .setListener(null);
        mProgressBar.setVisibility(View.GONE);
    }
}
