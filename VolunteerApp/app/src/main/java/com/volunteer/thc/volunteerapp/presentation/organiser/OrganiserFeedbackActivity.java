package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

public class OrganiserFeedbackActivity extends AppCompatActivity implements ActionListener.FeedbackDoneListener{
    private ArrayList<Volunteer> mVolunteers;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private RecyclerView mAcceptedUsersList;
    private TextView done, text1, text2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organiser_feedback);
        text1 = (TextView) findViewById(R.id.text1);
        text2 = (TextView) findViewById(R.id.text2);
        done = (TextView) findViewById(R.id.done);
        done.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (motionEvent.getRawY() >= done.getTotalPaddingTop()) {
                        finish();
                        return true;
                    }
                }
                return true;
            }
        });
        final String eventName = getIntent().getStringExtra("name");
        final ArrayList<String> mAcceptedUsers = getIntent().getStringArrayListExtra("volunteers");
        getSupportActionBar().setTitle(eventName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAcceptedUsersList = (RecyclerView) findViewById(R.id.feedbackRecView);
        mAcceptedUsersList.setHasFixedSize(true);

        for (final String volunteerID : mAcceptedUsers) {
            mVolunteers = new ArrayList<>();
            mDatabase.child("users").child("volunteers").child(volunteerID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Volunteer volunteer;
                    volunteer = dataSnapshot.getValue(Volunteer.class);
                    mVolunteers.add(volunteer);
                    if (TextUtils.equals(mAcceptedUsers.get(mAcceptedUsers.size() - 1), volunteerID)) {

                        EventFeedbackVolunteersAdapter adapter = new EventFeedbackVolunteersAdapter(mVolunteers, eventName, mAcceptedUsers, OrganiserFeedbackActivity.this);
                        mAcceptedUsersList.setAdapter(adapter);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(OrganiserFeedbackActivity.this);
                        mAcceptedUsersList.setLayoutManager(linearLayoutManager);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onFeedbackCompleted() {
        done.setVisibility(View.VISIBLE);
        text1.setVisibility(View.VISIBLE);
        text2.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
