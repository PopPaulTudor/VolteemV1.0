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
import com.volunteer.thc.volunteerapp.callback.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.type.EventType;
import com.volunteer.thc.volunteerapp.presentation.CreateEventActivity;
import com.volunteer.thc.volunteerapp.util.DatabaseUtils;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Cristi on 6/17/2017.
 */
public class OrganiserEventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ActionListener.EventPicturesLoadingListener {

    private static final String TAG = "OrgEventsF";
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
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
    private boolean mNoEventsDisplayed;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organiserevents, container, false);
        mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        fab = view.findViewById(R.id.add_event);
        recyclerView = view.findViewById(R.id.RecViewOrgEvents);
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
        noEvents = view.findViewById(R.id.no_events_text);
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
        mEventsList = new ArrayList<>();
        loadEvents();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        if (getActivity() == null) {
            return;
        }

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        if (searchManager == null) {
            return;
        }

        ComponentName componentName = new ComponentName(getActivity(), OrganiserSearchableActivity.class);
        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        MenuItem searchMenu = menu.findItem(R.id.app_bar_search);
        filter = menu.findItem(R.id.action_filter);
        actionFilter = filter.getActionView().findViewById(R.id.filterSpinner);
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
                    loadEvents();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing for now
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
        boolean result;
        // handle item selection
        switch (item.getItemId()) {
            case R.id.app_bar_search:
                if (getActivity() != null) {
                    getActivity().onSearchRequested();
                }
                result = true;
                break;
            case R.id.action_filter:
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }

        return result;
    }

    private void loadEvents() {
        noEvents.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(true);
        mEventsList = new ArrayList<>();

        if (isNetworkAvailable()) {
            mDatabase.child("events").orderByChild("created_by").equalTo(mUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot event : dataSnapshot.getChildren()) {
                                if ((filterType == null || TextUtils.equals(filterType, "All") || TextUtils.equals(
                                        String.valueOf(dataSnapshot.child("type").getValue()), filterType)) &&
                                        TextUtils.equals(String.valueOf(event.child("validity").getValue()), "valid")) {
                                    final Event currentEvent = event.getValue(Event.class);
                                    if (currentEvent == null) {
                                        continue;
                                    }

                                    ArrayList<String> reg_users = new ArrayList<>();
                                    ArrayList<String> acc_users = new ArrayList<>();

                                    for (DataSnapshot registered_users : event.child("users").getChildren()) {
                                        if (TextUtils.equals(String.valueOf(registered_users.child("status").getValue()), VolteemConstants
                                                .VOLUNTEER_EVENT_STATUS_PENDING)) {
                                            reg_users.add(String.valueOf(registered_users.child("id").getValue()));
                                        } else {
                                            acc_users.add(String.valueOf(registered_users.child("id").getValue()));
                                        }
                                    }

                                    currentEvent.setRegistered_volunteers(reg_users);
                                    currentEvent.setAccepted_volunteers(acc_users);
                                    if (currentEvent.getFinishDate() < date.getTimeInMillis()) {
                                        if (isFragmentActive()) {
                                            // TODO refactor so if there are many events not all are shown: 10 popups != best user experience
                                            mDatabase.child("users/organisers/" + mUser.getUid())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            long experience = dataSnapshot.child("experience").getValue(Long.class);
                                                            DatabaseUtils.writeData("users/organisers/" + mUser.getUid() + "/experience",
                                                                    (experience +
                                                                            (currentEvent.getSize() * 10)));
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                            mDatabase.child("events").child(currentEvent.getEventID()).child("validity").setValue("expired");
                                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                            alert.setTitle(getString(R.string.event_finished));
                                            alert.setCancelable(false);
                                            //TODO add message in strings with placeholders
                                            alert.setMessage("One of your events, " + currentEvent.getName() + ", has finished. " +
                                                    "If you have any feedback to give about any of the volunteers, please tap on the \"" +
                                                    "Give feedback\" button.");
                                            alert.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            alert.setNegativeButton(getString(R.string.give_feedback), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(getActivity(), OrganiserFeedbackActivity.class);
                                                    //TODO extract names to constants in VolteemConstants
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
                                mSwipeRefreshLayout.setRefreshing(false);

                                if (mEventsList.isEmpty() && getView() != null) {
                                    noEvents.setVisibility(View.VISIBLE);

                                    if (!mNoEventsDisplayed) {
                                        // TODO don't show this every single time...
                                        mNoEventsDisplayed = true;
                                        Snackbar snackbar = Snackbar.make(getView(), getString(R.string.organiser_no_events),
                                                Snackbar.LENGTH_LONG).setAction("Action", null);
                                        snackbar.setAction(getString(R.string.events_add), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                openCreateEventActivity();
                                            }
                                        });
                                        snackbar.show();
                                    }
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
                            // TODO handle errors
                            Log.e("Read", databaseError.getDetails());
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), getString(R.string.no_internet), Toast.LENGTH_LONG).show();
        }
    }

    public void openCreateEventActivity() {
        startActivity(new Intent(getActivity(), CreateEventActivity.class));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = getActivity() == null ? null : (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void populateSpinnerArray() {
        typeList.add("All");
        typeList.addAll(EventType.getAllAsList());
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
