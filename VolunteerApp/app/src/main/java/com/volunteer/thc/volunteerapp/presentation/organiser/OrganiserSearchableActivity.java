package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
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
import com.volunteer.thc.volunteerapp.adapter.OrganiserEventsAdapter;
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;
import java.util.List;

public class OrganiserSearchableActivity extends AppCompatActivity {

    private static final String TAG = "OrgSearchActivity";
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organiser_searchable);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final int animationTime = getResources().getInteger(android.R.integer.config_longAnimTime);
        final ProgressBar progressBar = findViewById(R.id.indeterminateBar);
        final RecyclerView recyclerView = findViewById(R.id.RecViewOrgSearchableEvents);
        recyclerView.setHasFixedSize(true);


        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            progressBar.setVisibility(View.VISIBLE);

            String query = intent.getStringExtra(SearchManager.QUERY);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(query);
            }

            final String lowercaseQuery = query.toLowerCase();
            mDatabase.child("events").orderByChild("created_by").equalTo(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // TODO check is activity is still visible before handling UI elements
                    List<Event> resultEvents = new ArrayList<>();
                    for (DataSnapshot event : dataSnapshot.getChildren()) {
                        Event currentEvent = event.getValue(Event.class);
                        if (currentEvent != null && currentEvent.getFinishDate() > CalendarUtil.getCurrentTimeInMillis()) {

                            String eventName = currentEvent.getName().toLowerCase();
                            if (eventName.contains(lowercaseQuery)) {
                                ArrayList<String> registeredUsers = new ArrayList<>();
                                ArrayList<String> acceptedUsers = new ArrayList<>();

                                for (DataSnapshot user : event.child("users").getChildren()) {
                                    if (TextUtils.equals(String.valueOf(user.child("status").getValue()), "pending")) {
                                        registeredUsers.add(String.valueOf(user.child("id").getValue()));
                                    } else {
                                        acceptedUsers.add(String.valueOf(user.child("id").getValue()));
                                    }
                                }

                                currentEvent.setRegistered_volunteers(registeredUsers);
                                currentEvent.setAccepted_volunteers(acceptedUsers);
                                resultEvents.add(currentEvent);
                            }
                        }
                    }

                    if (resultEvents.isEmpty()) {
                        TextView noResultText = findViewById(R.id.nothing_found_text);
                        noResultText.setVisibility(View.VISIBLE);
                    } else {
                        OrganiserEventsAdapter adapter = new OrganiserEventsAdapter(resultEvents,
                                OrganiserSearchableActivity.this, getResources(),
                                OrganiserEventsAdapter.MY_EVENTS, new ActionListener.EventPicturesLoadingListener() {
                            @Override
                            public void onPicturesLoaded() {
                                recyclerView.setAlpha(0f);
                                recyclerView.setVisibility(View.VISIBLE);
                                recyclerView.animate()
                                        .alpha(1f)
                                        .setDuration(animationTime)
                                        .setListener(null);
                            }
                        });

                        recyclerView.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OrganiserSearchableActivity.this);
                        recyclerView.setLayoutManager(linearLayoutManager);
                    }

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO handle exceptions
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, databaseError.getMessage());
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
