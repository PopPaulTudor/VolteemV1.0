package com.volunteer.thc.volunteerapp.presentation;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.Volunteer;

public class VolunteerSingleEventActivity extends AppCompatActivity {

    private TextView mEventName, mEventLocation, mEventDate, mEventType, mEventDescription, mEventDeadline, mEventSize;
    private Event currentEvent;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Button mSignUp;
    private ValueEventListener mRegisterListener;
    private int eventsNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_single_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");
        mSignUp = (Button) findViewById(R.id.event_signup);

        mEventName = (TextView) findViewById(R.id.event_name);
        mEventLocation = (TextView) findViewById(R.id.event_location);
        mEventDate = (TextView) findViewById(R.id.event_date);
        mEventType = (TextView) findViewById(R.id.event_type);
        mEventDescription = (TextView) findViewById(R.id.event_description);
        mEventDeadline = (TextView) findViewById(R.id.event_deadline);
        mEventSize = (TextView) findViewById(R.id.event_size);

        mEventName.setText(currentEvent.getName());
        mEventLocation.setText(currentEvent.getLocation());
        mEventDate.setText(currentEvent.getDate());
        mEventType.setText(currentEvent.getType());
        mEventDescription.setText(currentEvent.getDescription());
        mEventDeadline.setText(currentEvent.getDeadline());
        mEventSize.setText(currentEvent.getSize()+"");

        mRegisterListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventsNumber = (int) dataSnapshot.getChildrenCount();
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("events")
                        .child(eventsNumber+"").setValue(currentEvent.getEventID());
                Toast.makeText(VolunteerSingleEventActivity.this, "Sign up successful!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(VolunteerSingleEventActivity.this, "Signing up for event...", Toast.LENGTH_SHORT).show();
                mDatabase.child("events").child(currentEvent.getEventID()).child("registered_users").push()
                        .child("user").setValue(user.getUid());
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("events")
                        .addListenerForSingleValueEvent(mRegisterListener);
            }
        });
    }

}
