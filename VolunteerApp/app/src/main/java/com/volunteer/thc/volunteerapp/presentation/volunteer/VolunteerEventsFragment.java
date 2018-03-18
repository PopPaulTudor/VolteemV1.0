package com.volunteer.thc.volunteerapp.presentation.volunteer;


import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.volunteer.thc.volunteerapp.adapter.VolunteerEventsAdapter;
import com.volunteer.thc.volunteerapp.callback.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Cristi on 6/20/2017.
 */

public class VolunteerEventsFragment extends Fragment implements SwipeRefreshLayout
        .OnRefreshListener, ActionListener.EventPicturesLoadingListener {

    protected static boolean hasActionHappened = false;
    private List<Event> mEventsList = new ArrayList<>();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Calendar date = Calendar.getInstance();
    private TextView noEvents;
    private ArrayList<String> typeList = new ArrayList<>();
    private String filterType = "All";
    private Spinner actionFilter;
    private MenuItem filter;
    private int mLongAnimTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_volunteerevents, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.RecViewVolEvents);
        recyclerView.setHasFixedSize(true);
        noEvents = (TextView) view.findViewById(R.id.no_events_text);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        mLongAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

        loadEvents();
        setHasOptionsMenu(true);
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

    @Override
    public void onRefresh() {
        actionFilter.setSelection(typeList.indexOf("All"));
        loadEvents();
    }

    private void loadEvents() {

        mSwipeRefreshLayout.setRefreshing(true);
        noEvents.setVisibility(View.GONE);
        if (isNetworkAvailable()) {
            mDatabase.child("events").orderByChild("users/" + user.getUid()).equalTo(null)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mEventsList = new ArrayList<>();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        final Event currentEvent = data.getValue(Event.class);
                        if (currentEvent.getDeadline() > date.getTimeInMillis()) {
                            mEventsList.add(currentEvent);
                        }
                    }
                    if (mEventsList.isEmpty()) {
                        noEvents.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    if (isFragmentActive()) {
                        Collections.sort(mEventsList, new Comparator<Event>() {
                            @Override
                            public int compare(Event event, Event t1) {
                                if (event.getDeadline() < t1.getDeadline())
                                    return -1;
                                if (event.getDeadline() > t1.getDeadline())
                                    return 1;
                                return 0;
                            }
                        });

                        VolunteerEventsAdapter adapter = new VolunteerEventsAdapter(mEventsList,
                                getContext()
                                , getResources(), OrganiserEventsAdapter.ALL_EVENTS,
                                VolunteerEventsFragment.this, 1);
                        recyclerView.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager
                                (getActivity());
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("VolEventsF: loadEvents", databaseError.getMessage());
                }
            });

        } else {
            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadFilterQuery(final String filter) {
        mSwipeRefreshLayout.setRefreshing(true);
        mEventsList = new ArrayList<>();
        mDatabase.child("events").orderByChild("users/" + user.getUid()).equalTo(null)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (TextUtils.equals(dataSnapshot1.child("type").getValue().toString(),
                            filter) &&
                            dataSnapshot1.child("deadline").getValue(Long.class) > date
                                    .getTimeInMillis()) {
                        mEventsList.add(dataSnapshot1.getValue(Event.class));
                    }
                }
                if (isFragmentActive()) {

                    if (mEventsList.isEmpty()) {
                        noEvents.setVisibility(View.VISIBLE);
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    Collections.sort(mEventsList, new Comparator<Event>() {
                        @Override
                        public int compare(Event event, Event t1) {
                            if (event.getDeadline() < t1.getDeadline())
                                return -1;
                            if (event.getDeadline() > t1.getDeadline())
                                return 1;
                            return 0;
                        }
                    });

                    VolunteerEventsAdapter adapter = new VolunteerEventsAdapter(mEventsList,
                            getContext(),
                            getResources(), OrganiserEventsAdapter.ALL_EVENTS,
                            VolunteerEventsFragment
                            .this, 1);
                    recyclerView.setAdapter(adapter);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity
                            ());
                    recyclerView.setLayoutManager(linearLayoutManager);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("VolEventsF: loadFilterQ", databaseError.getMessage());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        ComponentName cn = new ComponentName(getActivity(), VolunteerSearchableActivity.class);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context
                .SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();

        MenuItem searchMenu = menu.findItem(R.id.app_bar_search);
        filter = menu.findItem(R.id.action_filter);
        actionFilter = (Spinner) filter.getActionView().findViewById(R.id.filterSpinner);
        populateSpinnerArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout
                .simple_spinner_dropdown_item, typeList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup
                    parent) {
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
                        filter.setVisible(false);
                        //get input method
                        InputMethodManager imm = (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
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
            case R.id.action_filter:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context
                .CONNECTIVITY_SERVICE);
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
        //TODO asta e metoda care se apeleaza pt animatie
        recyclerView.setAlpha(0f);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.animate()
                .alpha(1f)
                .setDuration(mLongAnimTime)
                .setListener(null);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
