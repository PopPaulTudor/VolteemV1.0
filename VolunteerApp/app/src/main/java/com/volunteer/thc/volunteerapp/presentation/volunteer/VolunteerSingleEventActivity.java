package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.RegisteredUser;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;

import java.util.ArrayList;

public class VolunteerSingleEventActivity extends AppCompatActivity {

    private TextView mEventName, mEventLocation, mEventType, mEventDescription, mEventDeadline, mEventSize, mStatus, mEventStartDate, mEventFinishDate;
    private Event currentEvent;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ValueEventListener mRegisterListener;
    private ArrayList<String> events = new ArrayList<>();
    private int eventsNumber;
    private ImageView collapsingToolbarImage;
    private Resources resources;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ArrayList<String> typeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_volunteer_single_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        currentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarImage = (ImageView) findViewById(R.id.collapsing_toolbar_image);

        resources = getResources();
        populateTypeList();
        populateUriList();

        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        storageRef.child("Photos").child("Event").child(currentEvent.getEventID()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Picasso.with(getApplicationContext()).load(task.getResult()).fit().centerCrop().into(collapsingToolbarImage);
                } else {
                    Picasso.with(getApplicationContext()).load(imageUris.get(typeList.indexOf(currentEvent.getType()))).fit().centerCrop().into(collapsingToolbarImage);
                }
            }
        });

        mEventName = (TextView) findViewById(R.id.event_name);
        mEventLocation = (TextView) findViewById(R.id.event_location);
        mEventStartDate = (TextView) findViewById(R.id.event_start_date);
        mEventFinishDate = (TextView) findViewById(R.id.event_finish_date);
        mEventType = (TextView) findViewById(R.id.event_type);
        mEventDescription = (TextView) findViewById(R.id.event_description);
        mEventDeadline = (TextView) findViewById(R.id.event_deadline);
        mEventSize = (TextView) findViewById(R.id.event_size);
        mStatus = (TextView) findViewById(R.id.event_status);

        FloatingActionButton mSignupForEventFloatingButton = (FloatingActionButton) findViewById(R.id.fab);
        Button mLeaveEvent = (Button) findViewById(R.id.event_leave);

        getSupportActionBar().setTitle(currentEvent.getName());

        mEventName.setText(currentEvent.getName());
        mEventLocation.setText(currentEvent.getLocation());
        mEventStartDate.setText(CalendarUtil.getStringDateFromMM(currentEvent.getStartDate()));
        mEventFinishDate.setText(CalendarUtil.getStringDateFromMM(currentEvent.getFinishDate()));
        mEventType.setText(currentEvent.getType());
        mEventDescription.setText(currentEvent.getDescription());
        mEventDeadline.setText(CalendarUtil.getStringDateFromMM(currentEvent.getDeadline()));
        mEventSize.setText(currentEvent.getSize() + " volunteers");

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);

        if (prefs.getInt("cameFrom", 1) == 1) {
            mSignupForEventFloatingButton.setVisibility(View.VISIBLE);
        } else {
            mStatus.setVisibility(View.VISIBLE);
            mLeaveEvent.setVisibility(View.VISIBLE);
            mDatabase.child("events").child(currentEvent.getEventID()).child("users").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (TextUtils.equals(dataSnapshot.child("status").getValue().toString(), "accepted")) {
                                mStatus.setText("Accepted");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

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
                VolunteerSearchableActivity.hasActionHappened = true;
                finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(VolunteerSingleEventActivity.this, "Sign up failed!", Toast.LENGTH_LONG).show();
            }
        };

        View.OnClickListener registerClickListener = new View.OnClickListener() {
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

                        VolunteerEventsFragment.hasActionHappened = true;
                        String newsID = mDatabase.child("news").push().getKey();
                        mDatabase.child("news/" + newsID).setValue(new NewsMessage(CalendarUtil.getCurrentTimeInMillis(),
                                newsID, currentEvent.getEventID(), user.getUid(), currentEvent.getCreated_by()
                                , "A new volunteer registered for your event " + currentEvent.getName()
                                , NewsMessage.REGISTERED, false, false));
                        mBottomSheetDialog.dismiss();
                        Toast.makeText(VolunteerSingleEventActivity.this, "Signing up for event...", Toast.LENGTH_SHORT).show();
                        mDatabase.child("events").child(currentEvent.getEventID()).child("users").child(user.getUid())
                                .setValue(new RegisteredUser("pending", user.getUid(), "valid"));
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
        };

        mSignupForEventFloatingButton.setOnClickListener(registerClickListener);

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

                        VolunteerMyEventsFragment.hasActionHappened = true;
                        mBottomSheetDialog.dismiss();
                        Toast.makeText(VolunteerSingleEventActivity.this, "Leaving event...", Toast.LENGTH_LONG).show();

                        mDatabase.child("events").child(currentEvent.getEventID()).child("users").child(user.getUid()).setValue(null);
                        events.remove(currentEvent.getEventID());
                        mDatabase.child("users").child("volunteers").child(user.getUid()).child("events").setValue(events);
                        Toast.makeText(VolunteerSingleEventActivity.this, "Event left.", Toast.LENGTH_LONG).show();
                        finish();
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

    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private Uri parseUri(int ID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + resources.getResourcePackageName(ID)
                + '/' + resources.getResourceTypeName(ID) + '/' + resources.getResourceEntryName(ID));

    }

    private void populateUriList() {
        imageUris.add(parseUri(R.drawable.image_sports));
        imageUris.add(parseUri(R.drawable.image_music));
        imageUris.add(parseUri(R.drawable.image_festival));
        imageUris.add(parseUri(R.drawable.image_charity));
        imageUris.add(parseUri(R.drawable.image_training));
        imageUris.add(parseUri(R.drawable.image_other));
    }

    private void populateTypeList() {
        typeList.add("Sports");
        typeList.add("Music");
        typeList.add("Festival");
        typeList.add("Charity");
        typeList.add("Training");
        typeList.add("Other");
    }
}
