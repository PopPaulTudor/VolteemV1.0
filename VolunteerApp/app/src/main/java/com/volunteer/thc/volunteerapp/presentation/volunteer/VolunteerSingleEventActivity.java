package com.volunteer.thc.volunteerapp.presentation.volunteer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.model.NewsMessage;
import com.volunteer.thc.volunteerapp.model.RegisteredUser;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;
import com.volunteer.thc.volunteerapp.util.CalendarUtil;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.io.File;
import java.util.ArrayList;

public class VolunteerSingleEventActivity extends AppCompatActivity {

    private TextView mEventName, mEventLocation, mEventType, mEventDescription, mEventDeadline,
            mEventSize, mStatus, mEventStartDate, mEventFinishDate;
    private Event currentEvent;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<String> events = new ArrayList<>();
    private ImageView collapsingToolbarImage;
    private Resources resources;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private ArrayList<String> typeList = new ArrayList<>();
    private FloatingActionButton mSignupForEventFloatingButton;
    private StorageReference storageRef;
    private Button mLeaveEvent, mDownloadContract;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private int mCameForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameForm = getIntent().getIntExtra(VolteemConstants
                .VOLUNTEER_SINGLE_ACTIVITY_CAME_FROM_KEY, 0);

        initActivityTransitions();
        setContentView(R.layout.activity_volunteer_single_event);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarImage = findViewById(R.id.collapsing_toolbar_image);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color
                .transparent));
        storageRef = FirebaseStorage.getInstance().getReference();

        resources = getResources();
        populateTypeList();
        populateUriList();

        mEventName = findViewById(R.id.event_name);
        mEventLocation = findViewById(R.id.event_location);
        mEventStartDate = findViewById(R.id.event_start_date);
        mEventFinishDate = findViewById(R.id.event_finish_date);
        mEventType = findViewById(R.id.event_type);
        mEventDescription = findViewById(R.id.event_description);
        mEventDeadline = findViewById(R.id.event_deadline);
        mEventSize = findViewById(R.id.event_size);
        mStatus = findViewById(R.id.event_status);


        mSignupForEventFloatingButton = findViewById(R.id.fab);
        mLeaveEvent = findViewById(R.id.event_leave);
        mDownloadContract = findViewById(R.id.event_pdf);
        currentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");

        if (currentEvent != null) {
            loadUI();
        } else {
            String eventID = getIntent().getStringExtra("eventID");
            if (eventID == null) {
                eventID = getIntent().getStringExtra("newsEventID");
            }
            mDatabase.child("events/" + eventID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currentEvent = dataSnapshot.getValue(Event.class);
                    loadUI();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("VolSingleEveA", databaseError.getMessage());
                }
            });
        }


        mDownloadContract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (PermissionUtil.isStorageWritePermissionGranted(VolunteerSingleEventActivity
                        .this)) {
                    final File rootPath = new File(Environment.getExternalStorageDirectory(),
                            "Volteem");
                    if (!rootPath.exists()) {
                        rootPath.mkdirs();
                    }
                    final File localFile = new File(rootPath, currentEvent.getName() + ".pdf");

                    storageRef.child("Contracts").child("Event").child(currentEvent.getEventId())
                            .getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            Snackbar.make(getCurrentFocus(), "Contract downloaded", Snackbar
                                    .LENGTH_LONG).setAction("Open Folder", new View
                                    .OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    // TODO: 29.10.2017 send user to path
                                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                    intent.setDataAndType(Uri.fromFile(localFile.getAbsoluteFile
                                            ()), "*/*");
                                    startActivity(Intent.createChooser(intent, "pdf"));

                                }
                            }).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e.toString().contains("does not exist at location")) {
                                Snackbar.make(getCurrentFocus(), "Contract is not uploaded yet. " +
                                                "We will notify you when the contract will be " +
                                                "available",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Snackbar.make(getCurrentFocus(), "Please allow storage permission", Snackbar
                            .LENGTH_LONG).setAction("Set Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(VolunteerSingleEventActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    }).show();
                }

            }
        });

        mSignupForEventFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog
                        (VolunteerSingleEventActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout
                        .event_register_bottom_sheet_design, null);
                mBottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View)
                        parentView.getParent());
                mBottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_DIP, 210, getResources().getDisplayMetrics()));
                mBottomSheetDialog.show();

                parentView.findViewById(R.id.register_event).setOnClickListener(new View
                        .OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        VolunteerEventsFragment.hasActionHappened = true;
                        String newsID = mDatabase.child("news").push().getKey();
                        mDatabase.child("news/" + newsID).setValue(new NewsMessage(CalendarUtil
                                .getCurrentTimeInMillis(),
                                newsID, currentEvent.getEventId(), user.getUid(), currentEvent
                                .getCreatedBy()
                                , "A new volunteer registered for your event " + currentEvent
                                .getName()
                                , NewsMessage.REGISTERED, false, false));
                        mBottomSheetDialog.dismiss();
                        Toast.makeText(VolunteerSingleEventActivity.this, "Signing up for " +
                                "event...", Toast.LENGTH_SHORT).show();
                        mDatabase.child("events").child(currentEvent.getEventId()).child("users")
                                .child(user.getUid())
                                .setValue(new RegisteredUser("pending", user.getUid(), "valid"));
                        finish();
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

                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog
                        (VolunteerSingleEventActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout
                        .leave_event_bottom_sheet_design, null);
                mBottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View)
                        parentView.getParent());
                mBottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_DIP, 210, getResources().getDisplayMetrics()));
                mBottomSheetDialog.show();

                parentView.findViewById(R.id.leave).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        VolunteerMyEventsFragment.hasActionHappened = true;
                        mBottomSheetDialog.dismiss();
                        Toast.makeText(VolunteerSingleEventActivity.this, "Leaving event...",
                                Toast.LENGTH_SHORT).show();
                        String newsID = mDatabase.child("news").push().getKey();
                        mDatabase.child("news/" + newsID).setValue(new NewsMessage(CalendarUtil
                                .getCurrentTimeInMillis(), newsID, currentEvent.getEventId(),
                                user.getUid(), currentEvent.getCreatedBy(), "A volunteer has " +
                                "left your event " + currentEvent.getName() + ".", NewsMessage
                                .VOLUNTEER_LEFT,
                                false, false));
                        mDatabase.child("events").child(currentEvent.getEventId()).child("users")
                                .child(user.getUid()).setValue(null);
                        events.remove(currentEvent.getEventId());
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

    private void loadUI() {
        storageRef.child("Photos").child("Event").child(currentEvent.getEventId()).getDownloadUrl
                ().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Picasso.with(getApplicationContext()).load(task.getResult()).fit()
                            .centerInside()
                            .into(collapsingToolbarImage);
                } else {
                    Picasso.with(getApplicationContext()).load(imageUris.get(typeList.indexOf
                            (currentEvent.getType()))).fit().centerCrop().into
                            (collapsingToolbarImage);
                }
            }
        });

        collapsingToolbarLayout.setTitle(currentEvent.getName());

        mEventName.setText(currentEvent.getName());
        mEventLocation.setText(currentEvent.getLocation());
        mEventStartDate.setText(CalendarUtil.getStringDateFromMM(currentEvent.getStartDate()));
        mEventFinishDate.setText(CalendarUtil.getStringDateFromMM(currentEvent.getFinishDate()));
        mEventType.setText(currentEvent.getType());
        mEventDescription.setText(currentEvent.getDescription());
        String deadline = CalendarUtil.getStringDateFromMM(currentEvent.getDeadline());
        mEventSize.setText(currentEvent.getSize() + " volunteers");

        int index = deadline.lastIndexOf("/");
        deadline = deadline.substring(0, index) + deadline.substring(index + 1);
        mEventDeadline.setText(deadline);

        if (mCameForm == 1) {
            mSignupForEventFloatingButton.setVisibility(View.VISIBLE);
        } else {
            mStatus.setVisibility(View.VISIBLE);
            mLeaveEvent.setVisibility(View.VISIBLE);

            mDatabase.child("events").child(currentEvent.getEventId()).child("users").child(user
                    .getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && TextUtils.equals(dataSnapshot.child
                                    ("status").getValue().toString(), "accepted")) {
                                mStatus.setText("Accepted");
                                mStatus.setTextColor(Color.rgb(25, 156, 136));
                                mDownloadContract.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("VolSingleEvA", databaseError.getMessage());
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
    public void onBackPressed() {
        if (getIntent().getStringExtra("eventID") != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
        super.onBackPressed();
    }

    private Uri parseUri(int ID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + resources.getResourcePackageName(ID)
                + '/' + resources.getResourceTypeName(ID) + '/' + resources.getResourceEntryName
                (ID));

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
