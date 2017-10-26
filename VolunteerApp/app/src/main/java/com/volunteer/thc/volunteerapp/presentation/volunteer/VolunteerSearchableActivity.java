package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SearchEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adaptor.OrgEventsAdaptor;
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;

public class VolunteerSearchableActivity extends AppCompatActivity implements ActionListener.EventPicturesLoadingListener{

    private String query;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private RecyclerView recyclerView;
    private ArrayList<Event> mResultEvents = new ArrayList<>();
    private ProgressBar mProgressBar;
    private TextView mNoResultText;
    private int mLongAnimTime;
    public static boolean hasActionHappened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_searchable);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView) findViewById(R.id.RecViewVolSearchableEvents);
        recyclerView.setHasFixedSize(true);

        mLongAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

        mProgressBar = (ProgressBar) findViewById(R.id.indeterminateBar);
        mNoResultText = (TextView) findViewById(R.id.nothing_found_text);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle(query);
            query = query.toLowerCase();

            //TODO the line bellow sometimes makes app crash
            Answers.getInstance().logSearch(new SearchEvent().putQuery("SearchEvent")
                    .putCustomAttribute("Query", query));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasActionHappened) {
            hasActionHappened = false;
            if (mResultEvents.size() == 1) {
                finish();
            }
        }
        loadResultEvents();
    }

    private void loadResultEvents() {
        mProgressBar.setVisibility(View.VISIBLE);

        mResultEvents = new ArrayList<>();
        mDatabase.child("events").orderByChild("users/" + user.getUid()).equalTo(null).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    Event currentEvent = dataSnapshot1.getValue(Event.class);
                    String eventName = currentEvent.getName().toLowerCase();
                    if (eventName.contains(query) && currentEvent.getDeadline() > CalendarUtil.getCurrentTimeInMillis()) {
                        mResultEvents.add(currentEvent);
                    }
                }
                if (mResultEvents.isEmpty()) {
                    mNoResultText.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                    Log.w("VolunteerSearchQuery", " nothing found");
                }  else {
                    Log.w("VolunteerSearchQuery", " event(s) found");
                }
                OrgEventsAdaptor adapter = new OrgEventsAdaptor(mResultEvents, VolunteerSearchableActivity.this, getResources(), OrgEventsAdaptor.ALL_EVENTS, VolunteerSearchableActivity.this);
                recyclerView.setAdapter(adapter);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(VolunteerSearchableActivity.this);
                recyclerView.setLayoutManager(linearLayoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
