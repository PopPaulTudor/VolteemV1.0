package com.volunteer.thc.volunteerapp.presentation;

import android.app.SearchManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class VolunteerSearchableActivity extends AppCompatActivity {

    private String query;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private RecyclerView recyclerView;
    private List<Event> mResultEvents = new ArrayList<>();
    private List<String> mUserEvents = new ArrayList<>();
    private ProgressBar mProgressBar;
    private TextView mNoResultText;
    private ValueEventListener mRetrieveEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_searchable);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.RecViewVolSearchableEvents);
        recyclerView.setHasFixedSize(true);

        mProgressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        mNoResultText = (TextView) findViewById(R.id.nothing_found_text);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle(query);
            query = query.toLowerCase();

            mProgressBar.setVisibility(View.VISIBLE);

            mDatabase.child("users").child("volunteers").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
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
                    mResultEvents = new ArrayList<>();
                    for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {

                        String eventName = eventSnapshot.child("name").getValue().toString();
                        eventName = eventName.toLowerCase();
                        Event currentEvent = eventSnapshot.getValue(Event.class);
                        if (!isUserRegisteredForEvent(currentEvent.getEventID()) && eventName.contains(query)) {
                            mResultEvents.add(currentEvent);
                        }
                    }

                    mProgressBar.setVisibility(View.GONE);
                    if(mResultEvents.isEmpty()) {
                        mNoResultText.setVisibility(View.VISIBLE);
                    } else {
                        OrgEventsAdaptor adapter = new OrgEventsAdaptor(mResultEvents, VolunteerSearchableActivity.this);
                        recyclerView.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(VolunteerSearchableActivity.this);
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private boolean isUserRegisteredForEvent(String eventID) {

        for(String event: mUserEvents) {
            if(TextUtils.equals(eventID, event)) {
                return true;
            }
        }
        return false;
    }
}
