package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.adapter.EventFeedbackVolunteersAdapter;
import com.volunteer.thc.volunteerapp.interrface.ActionListener;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import java.util.ArrayList;

public class OrganiserFeedbackActivity extends AppCompatActivity {

    private static final String TAG = "OrgFeedbackActivity";
    private ArrayList<Volunteer> mVolunteers;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private RecyclerView mAcceptedUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organiser_feedback);

        final String eventName = getIntent().getStringExtra("name");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(eventName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final TextView feedbackThxMsg = findViewById(R.id.feedback_thx);
        final TextView feedbackBack = findViewById(R.id.feedback_back);
        final TextView feedbackDone = findViewById(R.id.feedback_done);

        feedbackDone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getRawY() >= feedbackDone.getTotalPaddingTop()) {
                        finish();
                    }
                }
                return true;
            }
        });

        mAcceptedUsersList = findViewById(R.id.feedbackRecView);
        mAcceptedUsersList.setHasFixedSize(true);

        final ArrayList<String> acceptedUsers = getIntent().getStringArrayListExtra("volunteers");
        for (final String volunteerID : acceptedUsers) {
            mVolunteers = new ArrayList<>();
            mDatabase.child("users").child("volunteers").child(volunteerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mVolunteers.add(dataSnapshot.getValue(Volunteer.class));
                    if (TextUtils.equals(acceptedUsers.get(acceptedUsers.size() - 1), volunteerID)) {
                        EventFeedbackVolunteersAdapter adapter = new EventFeedbackVolunteersAdapter(mVolunteers, eventName, acceptedUsers,
                                new ActionListener.FeedbackDoneListener() {
                                    @Override
                                    public void onFeedbackCompleted() {
                                        feedbackDone.setVisibility(View.VISIBLE);
                                        feedbackThxMsg.setVisibility(View.VISIBLE);
                                        feedbackBack.setVisibility(View.VISIBLE);
                                    }
                                });
                        mAcceptedUsersList.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OrganiserFeedbackActivity.this);
                        mAcceptedUsersList.setLayoutManager(linearLayoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // TODO error handling
                    Log.w(TAG, databaseError.getMessage());
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
