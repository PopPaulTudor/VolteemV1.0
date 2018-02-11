package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;
import java.util.List;

public class OrganiserSearchableActivity extends AppCompatActivity implements ActionListener.EventPicturesLoadingListener {

    private String query;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private RecyclerView recyclerView;
    private List<Event> mResultEvents = new ArrayList<>();
    private TextView mNoResultText;
    private ProgressBar mProgressBar;
    private int mLongAnimTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organiser_searchable);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.RecViewOrgSearchableEvents);
        recyclerView.setHasFixedSize(true);

        mLongAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);

        mNoResultText = (TextView) findViewById(R.id.nothing_found_text);
        mProgressBar = (ProgressBar) findViewById(R.id.indeterminateBar);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle(query);
            query = query.toLowerCase();

            mProgressBar.setVisibility(View.VISIBLE);

            mDatabase.child("events").orderByChild("created_by").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot event : dataSnapshot.getChildren()) {
                        Event currentEvent = event.getValue(Event.class);
                        if (currentEvent.getFinishDate() > CalendarUtil.getCurrentTimeInMillis()) {

                            String eventName = currentEvent.getName().toLowerCase();
                            if (eventName.contains(query)) {

                                ArrayList<String> reg_users = new ArrayList<>();
                                ArrayList<String> acc_users = new ArrayList<>();

                                for (DataSnapshot registered_users : event.child("users").getChildren()) {
                                    if (TextUtils.equals(registered_users.child("status").getValue().toString(), "pending")) {
                                        reg_users.add(registered_users.child("id").getValue().toString());
                                    } else {
                                        acc_users.add(registered_users.child("id").getValue().toString());
                                    }
                                }
                                currentEvent.setRegistered_volunteers(reg_users);
                                currentEvent.setAccepted_volunteers(acc_users);
                                mResultEvents.add(currentEvent);
                            }
                        }
                    }
                    if (mResultEvents.isEmpty()) {
                        mNoResultText.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    } else {
                        OrgEventsAdaptor adapter = new OrgEventsAdaptor(mResultEvents,
                                OrganiserSearchableActivity.this, getResources(),
                                OrgEventsAdaptor.MY_EVENTS, OrganiserSearchableActivity.this, 1);
                        recyclerView.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OrganiserSearchableActivity.this);
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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
