package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.OrganiserEventsAdapter;
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.CreateEventActivity;
import com.volunteer.thc.volunteerapp.util.DatabaseUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Cristi on 6/17/2017.
 */

public class OrganiserEventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ActionListener.EventPicturesLoadingListener {

    public static boolean hasActionHappened = false;
    private final String pending = "pending";
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<Event> mEventsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Calendar date = Calendar.getInstance();
    private TextView noEvents;
    private Spinner actionFilter;
    private ArrayList<String> typeList = new ArrayList<>();
    private FloatingActionButton fab;
    private MenuItem filter;
    private String filterType = "All";
    private int mLongAnimTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserevents, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        fab = (FloatingActionButton) view.findViewById(R.id.add_event);
        recyclerView = (RecyclerView) view.findViewById(R.id.RecViewOrgEvents);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown()) {
                    fab.hide();
                } else {
                    if (dy < 0 && !fab.isShown()) {
                        fab.show();
                    }
                }
            }
        });
        noEvents = (TextView) view.findViewById(R.id.no_events_text);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

        mLongAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        view.findViewById(R.id.add_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: after testing is over: remember to only allow organisers which verified their email address to add events
                openCreateEventActivity();
            }
        });

        loadEvents();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onRefresh() {
        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (hasActionHappened) {
            mEventsList = new ArrayList<>();
            loadEvents();
            hasActionHappened = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        ComponentName cn = new ComponentName(getActivity(), OrganiserSearchableActivity.class);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        MenuItem searchMenu = menu.findItem(R.id.app_bar_search);
        filter = menu.findItem(R.id.action_filter);
        actionFilter = (Spinner) filter.getActionView().findViewById(R.id.filterSpinner);
        populateSpinnerArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, typeList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setVisibility(View.GONE);
                return view;
            }
        };
        actionFilter.setAdapter(adapter);
        actionFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!TextUtils.equals(filterType, actionFilter.getSelectedItem().toString())) {
                    filterType = actionFilter.getSelectedItem().toString();
                    noEvents.setVisibility(View.GONE);
                    if (TextUtils.equals(filterType, "All")) {
                        loadEvents();
                    } else {
                        loadFilterQuery(filterType);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        MenuItemCompat.setOnActionExpandListener(searchMenu,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        item.getActionView().clearFocus();
                        filter.setVisible(true);
                        return true;  // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        //get focus
                        item.getActionView().requestFocus();
                        //get input method
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        filter.setVisible(false);
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
            case R.id.action_filter:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadEvents() {

        noEvents.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mEventsList = new ArrayList<>();

        if (isNetworkAvailable()) {

            mDatabase.child("events").orderByChild("created_by").equalTo(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot event : dataSnapshot.getChildren()) {
                                if (TextUtils.equals(event.child("validity").getValue().toString(), "valid")) {
                                    final Event currentEvent = event.getValue(Event.class);
                                    ArrayList<String> reg_users = new ArrayList<>();
                                    ArrayList<String> acc_users = new ArrayList<>();

                                    for (DataSnapshot registered_users : event.child("users").getChildren()) {
                                        if (TextUtils.equals(registered_users.child("status").getValue().toString(), pending)) {
                                            reg_users.add(registered_users.child("id").getValue().toString());
                                        } else {
                                            acc_users.add(registered_users.child("id").getValue().toString());
                                        }
                                    }
                                    currentEvent.setRegistered_volunteers(reg_users);
                                    currentEvent.setAccepted_volunteers(acc_users);
                                    if (currentEvent.getFinishDate() < date.getTimeInMillis()) {
                                        if (isFragmentActive()) {
                                            mDatabase.child("users/organisers/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    long experience = dataSnapshot.child("experience").getValue(Long.class);
                                                    DatabaseUtils.writeData("users/organisers/" + user.getUid() + "/experience", (experience + (currentEvent.getSize() * 10)));
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            mDatabase.child("events").child(currentEvent.getEventID()).child("validity").setValue("expired");
                                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                            alert.setTitle("Event finished");
                                            alert.setCancelable(false);
                                            alert.setMessage("One of your events, " + currentEvent.getName() + ", has finished. " +
                                                    "If you have any feedback to give about any of the volunteers, please tap on the \"" +
                                                    "Give feedback\" button.");
                                            alert.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            alert.setNegativeButton("GIVE FEEDBACK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getActivity(), OrganiserFeedbackActivity.class);
                                                    intent.putExtra("name", currentEvent.getName());
                                                    intent.putExtra("volunteers", currentEvent.getAccepted_volunteers());
                                                    startActivity(intent);
                                                    dialogInterface.dismiss();
                                                }
                                            });
                                            alert.show();
                                        }

                                    } else {
                                        mEventsList.add(currentEvent);
                                    }
                                }
                            }

                            if (isFragmentActive()) {
                                mSwipeRefreshLayout.setRefreshing(true);
                                if (mEventsList.isEmpty()) {

                                    noEvents.setVisibility(View.VISIBLE);
                                    Snackbar snackbar = Snackbar.make(getView(), "You don't have any events. How about creating one now?", Snackbar.LENGTH_LONG).setAction("Action", null);
                                    snackbar.setAction("Add", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            openCreateEventActivity();
                                        }
                                    });
                                    snackbar.show();
                                }

                                OrganiserEventsAdapter adapter = new OrganiserEventsAdapter
                                        (mEventsList,
                                                getContext(), getResources(), OrganiserEventsAdapter
                                                .MY_EVENTS,
                                        OrganiserEventsFragment.this);
                                recyclerView.setAdapter(adapter);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                recyclerView.setLayoutManager(linearLayoutManager);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("Read", databaseError.getDetails());
                        }
                    });

        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadFilterQuery(final String filter) {
        noEvents.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mEventsList = new ArrayList<>();
        mDatabase.child("events").orderByChild("created_by").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (TextUtils.equals(dataSnapshot1.child("type").getValue().toString(), filter) &&
                            TextUtils.equals(dataSnapshot1.child("validity").getValue().toString(), "valid")) {
                        final Event currentEvent = dataSnapshot1.getValue(Event.class);
                        ArrayList<String> reg_users = new ArrayList<>();
                        ArrayList<String> acc_users = new ArrayList<>();

                        for (DataSnapshot registered_users : dataSnapshot1.child("users").getChildren()) {
                            if (TextUtils.equals(registered_users.child("status").getValue().toString(), pending)) {
                                reg_users.add(registered_users.child("id").getValue().toString());
                            } else {
                                acc_users.add(registered_users.child("id").getValue().toString());
                            }
                        }
                        currentEvent.setRegistered_volunteers(reg_users);
                        currentEvent.setAccepted_volunteers(acc_users);

                        mEventsList.add(currentEvent);
                    }
                }
                if (isFragmentActive()) {

                    if (mEventsList.isEmpty()) {
                        noEvents.setVisibility(View.VISIBLE);
                    }

                    OrganiserEventsAdapter adapter = new OrganiserEventsAdapter(mEventsList,
                            getContext(), getResources(), OrganiserEventsAdapter.MY_EVENTS,
                            OrganiserEventsFragment.this);
                    recyclerView.setAdapter(adapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                    recyclerView.setLayoutManager(linearLayoutManager);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("OrgEventsF", databaseError.getMessage());
            }
        });
    }

    public void openCreateEventActivity() {
        startActivity(new Intent(getActivity(), CreateEventActivity.class));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void populateSpinnerArray() {
        typeList.add("All");
        typeList.add("Sports");
        typeList.add("Music");
        typeList.add("Festival");
        typeList.add("Charity");
        typeList.add("Training");
        typeList.add("Other");
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
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
