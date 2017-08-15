package com.volunteer.thc.volunteerapp.presentation.volunteer;


import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.RatingBar;
import android.widget.SearchView;
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
 * Created by Cristi on 6/20/2017.
 */

public class VolunteerEventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private List<Event> mEventsList = new ArrayList<>();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerView;
    private ValueEventListener mRetrieveEvents;
    private ArrayList<String> mUserEvents = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Calendar date = Calendar.getInstance();
    private TextView noEvents;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteerevents, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.RecViewVolEvents);
        recyclerView.setHasFixedSize(true);
        noEvents = (TextView) view.findViewById(R.id.no_events_text);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.colorPrimary);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadEvents();
    }

    @Override
    public void onRefresh() {
        loadEvents();
    }

    private void loadEvents() {

        mSwipeRefreshLayout.setRefreshing(true);
        if (isNetworkAvailable()) {

            mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUserEvents = new ArrayList<>();
                    for (DataSnapshot usersSnapshot : dataSnapshot.child("events").getChildren()) {
                        mUserEvents.add(usersSnapshot.getValue().toString());
                    }
                    mDatabase.child("events").addListenerForSingleValueEvent(mRetrieveEvents);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mRetrieveEvents = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mEventsList = new ArrayList<>();
                    boolean anyEventsExpired = false;
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                        boolean isUserAccepted = false;
                        final Event currentEvent = eventSnapshot.getValue(Event.class);
                        boolean isUserRegistered = isUserRegisteredForEvent(currentEvent.getEventID());
                        if (!isUserRegistered && (currentEvent.getDeadline() > date.getTimeInMillis())) {
                            mEventsList.add(currentEvent);
                        } else {
                            if (isUserRegistered && currentEvent.getFinishDate() < date.getTimeInMillis()) {
                                anyEventsExpired = true;
                                mUserEvents.remove(currentEvent.getEventID());
                                for (DataSnapshot accepted_users : eventSnapshot.child("accepted_users").getChildren()) {
                                    if (TextUtils.equals(accepted_users.child("user").getValue().toString(), user.getUid())) {
                                        isUserAccepted = true;
                                        break;
                                    }
                                }
                                if (isUserAccepted) {
                                    mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            long experience = dataSnapshot.child("experience").getValue(Long.class);
                                            int past_events_nr = (int) dataSnapshot.child("past_events").getChildrenCount();
                                            mDatabase.child("users").child("volunteers").child(user.getUid()).child("past_events")
                                                    .child(past_events_nr + "").setValue(currentEvent.getEventID());
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
                    if (anyEventsExpired) {
                        mDatabase.child("users").child("volunteers").child(user.getUid()).child("events").setValue(mUserEvents);
                    }
                    if (mEventsList.isEmpty()) {
                        noEvents.setVisibility(View.VISIBLE);
                    }
                    mSwipeRefreshLayout.setRefreshing(false);
                    OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList, getContext());
                    recyclerView.setAdapter(adapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(linearLayoutManager);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

        } else {

            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);

        ComponentName cn = new ComponentName(getActivity(), VolunteerSearchableActivity.class);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        MenuItem searchMenu = menu.findItem(R.id.app_bar_search);
        MenuItemCompat.setOnActionExpandListener(searchMenu,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        item.getActionView().clearFocus();
                        return true;  // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        //get focus
                        item.getActionView().requestFocus();
                        //get input method
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        return true;  // Return true to expand action view
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.app_bar_search:
                getActivity().onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isUserRegisteredForEvent(String eventID) {
        for (String event : mUserEvents) {
            if (TextUtils.equals(eventID, event)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
