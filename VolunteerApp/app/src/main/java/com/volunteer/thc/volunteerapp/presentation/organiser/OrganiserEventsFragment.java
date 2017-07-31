package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.OrgEventsAdaptor;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.CreateEventFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cristi on 6/17/2017.
 */

public class OrganiserEventsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private List<Event> mEventsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserevents, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) view.findViewById(R.id.RecViewOrgEvents);
        recyclerView.setHasFixedSize(true);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.colorPrimary);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });

        view.findViewById(R.id.add_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateEventFragment();
            }
        });
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onRefresh() {
        mEventsList = new ArrayList<>();
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
        inflater.inflate(R.menu.search_menu, menu);

        ComponentName cn = new ComponentName(getActivity(), OrganiserSearchableActivity.class);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        searchView.setIconifiedByDefault(false);
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

    private void loadEvents() {

        mSwipeRefreshLayout.setRefreshing(true);

        if (isNetworkAvailable()) {

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

                            mSwipeRefreshLayout.setRefreshing(false);

                            if (mEventsList.isEmpty()) {

                                Snackbar snackbar = Snackbar.make(getView(), "You don't have any events. How about creating one now?", Snackbar.LENGTH_LONG).setAction("Action", null);
                                snackbar.setAction("Add", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        openCreateEventFragment();
                                    }
                                });
                                snackbar.show();

                            } else {

                                OrgEventsAdaptor adapter = new OrgEventsAdaptor(mEventsList, getContext());
                                recyclerView.setAdapter(adapter);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                                recyclerView.setLayoutManager(linearLayoutManager);

                                Activity activity = getActivity();
                                if (activity != null) {
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

            mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), "No internet connection.", Toast.LENGTH_LONG).show();
        }
    }

    public void openCreateEventFragment() {

        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            FragmentTransaction mFragmentTransaction = fragmentManager.beginTransaction();
            mFragmentTransaction.replace(R.id.main_container, new CreateEventFragment());
            mFragmentTransaction.addToBackStack("createEvent");
            mFragmentTransaction.commit();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
