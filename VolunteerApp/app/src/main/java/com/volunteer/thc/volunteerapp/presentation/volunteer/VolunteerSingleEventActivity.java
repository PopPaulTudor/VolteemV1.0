package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
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

import java.util.ArrayList;

public class VolunteerSingleEventActivity extends AppCompatActivity {

    private TextView mEventName, mEventLocation, mEventDate, mEventType, mEventDescription, mEventDeadline, mEventSize, mStatus;
    private Event currentEvent;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Button mSignupForEvent, mLeaveEvent;
    private ValueEventListener mRegisterListener;
    private ArrayList<String> events = new ArrayList<>();
    private int eventsNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_single_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");

        mEventName = (TextView) findViewById(R.id.event_name);
        mEventLocation = (TextView) findViewById(R.id.event_location);
        mEventDate = (TextView) findViewById(R.id.event_date);
        mEventType = (TextView) findViewById(R.id.event_type);
        mEventDescription = (TextView) findViewById(R.id.event_description);
        mEventDeadline = (TextView) findViewById(R.id.event_deadline);
        mEventSize = (TextView) findViewById(R.id.event_size);
        mStatus = (TextView) findViewById(R.id.event_status);

        mSignupForEvent = (Button) findViewById(R.id.event_signup);
        mLeaveEvent = (Button) findViewById(R.id.event_leave);

        getSupportActionBar().setTitle(currentEvent.getName());

        mEventName.setText("Name: " + currentEvent.getName());
        mEventLocation.setText("Location: " + currentEvent.getLocation());
        mEventDate.setText("Date: " + currentEvent.getDate());
        mEventType.setText("Type: " + currentEvent.getType());
        mEventDescription.setText("Description: " + currentEvent.getDescription());
        mEventDeadline.setText("Deadline: " + currentEvent.getDeadline());
        mEventSize.setText("Volunteers needed: " + currentEvent.getSize() + "");

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        if (prefs.getInt("cameFrom", 1) == 1) {
            mSignupForEvent.setVisibility(View.VISIBLE);
        } else {
            mStatus.setVisibility(View.VISIBLE);
            mLeaveEvent.setVisibility(View.VISIBLE);
        }

        mDatabase.child("events").child(currentEvent.getEventID()).child("accepted_users").orderByChild("user")
                .startAt(user.getUid()).endAt(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    mStatus.setText("Status: Accepted");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mDatabase.child("users").child("volunteers").child(user.getUid()).child("events")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            events.add(data.getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        mRegisterListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventsNumber = (int) dataSnapshot.getChildrenCount();
                mDatabase.child("users").child("volunteers").child(user.getUid()).child("events")
                        .child(eventsNumber + "").setValue(currentEvent.getEventID());
                Toast.makeText(VolunteerSingleEventActivity.this, "Sign up successful!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(VolunteerSingleEventActivity.this, "Sign up failed!", Toast.LENGTH_LONG).show();
            }
        };

        mSignupForEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(VolunteerSingleEventActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout.event_register_bottom_sheet_design, null);
                mBottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                mBottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_DIP, 210, getResources().getDisplayMetrics()));
                mBottomSheetDialog.show();

                parentView.findViewById(R.id.register_event).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mBottomSheetDialog.dismiss();
                        Toast.makeText(VolunteerSingleEventActivity.this, "Signing up for event...", Toast.LENGTH_SHORT).show();
                        mDatabase.child("events").child(currentEvent.getEventID()).child("registered_users").push()
                                .child("user").setValue(user.getUid());
                        mDatabase.child("users").child("volunteers").child(user.getUid()).child("events")
                                .addListenerForSingleValueEvent(mRegisterListener);
                    }
                });

                parentView.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mBottomSheetDialog.dismiss();
                    }
                });
            }
        });

        mLeaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(VolunteerSingleEventActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout.leave_event_bottom_sheet_design, null);
                mBottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                mBottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_DIP, 210, getResources().getDisplayMetrics()));
                mBottomSheetDialog.show();

                parentView.findViewById(R.id.leave).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mBottomSheetDialog.dismiss();
                        Toast.makeText(VolunteerSingleEventActivity.this, "Leaving event...", Toast.LENGTH_LONG).show();

                        mDatabase.child("events").child(currentEvent.getEventID()).child("registered_users").orderByChild("user")
                                .startAt(user.getUid()).endAt(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    mDatabase.child("events").child(currentEvent.getEventID()).child("registered_users").child(data.getKey()).setValue(null);
                                }
                                events.remove(currentEvent.getEventID());
                                mDatabase.child("users").child("volunteers").child(user.getUid()).child("events").setValue(events);
                                Toast.makeText(VolunteerSingleEventActivity.this, "Event left.", Toast.LENGTH_LONG).show();
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(VolunteerSingleEventActivity.this, "Leaving event failed.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                parentView.findViewById(R.id.stay).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mBottomSheetDialog.dismiss();
                    }
                });
            }
        });
    }
}
