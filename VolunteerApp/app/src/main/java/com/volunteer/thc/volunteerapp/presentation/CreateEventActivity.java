package com.volunteer.thc.volunteerapp.presentation;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.volunteer.thc.volunteerapp.model.type.EventType;
import com.volunteer.thc.volunteerapp.adapter.EventQuestionsAdapter;
import com.volunteer.thc.volunteerapp.model.InterviewQuestion;
import com.volunteer.thc.volunteerapp.notification.NotificationEventReceiver;
import com.volunteer.thc.volunteerapp.util.DatabaseUtils;
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;
import com.volunteer.thc.volunteerapp.util.VolteemConstants;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created on 6/23/2017.
 */

public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEventActivity";
    private static final int GALLERY_INTENT = 1;
    private static final int PICK_PDF = 2;
    private static final int NUMBER_OF_EVENTS_ON_PAGE = 3;
    private EditText mName, mLocation, mDescription, mDeadline, mSize, mStartDate, mFinishDate;
    private ImageView mImage;
    private Spinner mTypeSpinner; // TODO refactor spinner and use some popup dialog
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private long startDate = -1, finishDate, deadline;
    private StorageReference mStorage;
    private Uri mUriPicture = null, mUriPDF = null;
    private ArrayList<String> typeList = new ArrayList<>();
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private boolean mSelectedPicture = false;
    private boolean mSelectedPDF = false;
    private Button mLoadPdf, mDoneButton;
    private ArrayList<InterviewQuestion> questionsList = new ArrayList<>();
    private Resources resources;
    private TextView questionText;
    private int longAnimTime;
    private EventQuestionsAdapter eventQuestionsAdapter;
    private ScrollView createEventScrollView;
    private NestedScrollView questionsScrollView;
    private RecyclerView questionsRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration conf = getResources().getConfiguration();
        if (conf.smallestScreenWidthDp >= 600) { // now it is at least a tablet with 7'in
            if (conf.smallestScreenWidthDp >= 720) { // now it is a 10'in tablet
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR); // Or portrait
            } else { // now it is a 7'in tablet
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        } else { // now it is a regular device below 7`in
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        setContentView(R.layout.activity_create_event);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.new_event));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mName = findViewById(R.id.event_deadline);
        mLocation = findViewById(R.id.event_location);
        mStartDate = findViewById(R.id.event_date_start_create);
        mFinishDate = findViewById(R.id.event_date_finish_create);
        mTypeSpinner = findViewById(R.id.event_type);
        mDescription = findViewById(R.id.event_description);
        mDeadline = findViewById(R.id.event_deadline_create);
        mImage = findViewById(R.id.event_image);
        mSize = findViewById(R.id.event_size);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDoneButton = (Button) findViewById(R.id.questions_done);
        questionText = (TextView) findViewById(R.id.question_text);
        createEventScrollView = (ScrollView) findViewById(R.id.create_event);
        questionsScrollView = (NestedScrollView) findViewById(R.id.questionsScrollView);
        questionsRecyclerView = (RecyclerView) findViewById(R.id.questions_recyclerView);
        questionsRecyclerView.setHasFixedSize(true);
        resources = getResources();
        longAnimTime = resources.getInteger(android.R.integer.config_longAnimTime);
        populateUriList();
        populateSpinnerArray();

        Picasso.get().load(imageUris.get(NUMBER_OF_EVENTS_ON_PAGE)).fit().centerCrop().into(mImage);
        Button mSaveEvent = findViewById(R.id.save_event);
        Button mCancel = findViewById(R.id.cancel_event);
        mLoadPdf = findViewById(R.id.upload_pdf);

        mDatabase.child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    questionsList.add(dataSnapshot1.getValue(InterviewQuestion.class));
                }
                eventQuestionsAdapter = new EventQuestionsAdapter(questionsList);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CreateEventActivity.this);
                questionsRecyclerView.setAdapter(eventQuestionsAdapter);
                questionsRecyclerView.setLayoutManager(linearLayoutManager);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("CreateEventQuestions", databaseError.getMessage());
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, typeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner.setAdapter(adapter);

        mTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!mSelectedPicture) {
                    Picasso.get().load(imageUris.get(mTypeSpinner.getSelectedItemPosition())).fit().centerCrop().into
                            (mImage);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtil.isStorageReadPermissionGranted(CreateEventActivity.this)) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);
                } else {
                    Snackbar.make(view, getString(R.string.allow_storge_permission), Snackbar.LENGTH_LONG).setAction(getString(R.string
                            .set_permission), new View
                            .OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(CreateEventActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    VolteemConstants.STORAGE_REQUEST_CODE);
                        }
                    }).show();
                }
            }
        });

        mLoadPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtil.isStorageReadPermissionGranted(CreateEventActivity.this)) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("application/pdf");
                    startActivityForResult(intent, PICK_PDF);
                } else {
                    Snackbar.make(view, getString(R.string.allow_storge_permission), Snackbar.LENGTH_LONG).setAction(getString(R.string
                            .set_permission), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(CreateEventActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    VolteemConstants.STORAGE_REQUEST_CODE);
                        }
                    }).show();
                }
            }
        });

        mStartDate.setOnClickListener(setOnClickListenerCalendar(mStartDate));
        mFinishDate.setOnClickListener(setOnClickListenerCalendar(mFinishDate));
        mDeadline.setOnClickListener(setOnClickListenerCalendar(mDeadline));

        mSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide keyboard on save event pressed
                hideKeyboardFrom(CreateEventActivity.this, view);

                if (validateForm()) {

                    hideKeyboardFrom(CreateEventActivity.this, view);
                    animateQuestions();

                    mDoneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            final ArrayList<String> selectedQuestions = eventQuestionsAdapter.getSelectedQuestionsList();
                            if (selectedQuestions.isEmpty()) {
                                AlertDialog noQuestionsAlertDialog = new AlertDialog.Builder(CreateEventActivity.this)
                                        .setTitle("Are you sure?")
                                        .setMessage("Are you sure you want to save this event without creating a questions form?")
                                        .setCancelable(true)
                                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ///do nothing
                                            }
                                        })
                                        .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                createEvent(selectedQuestions);
                                                Snackbar.make(view, "Event created!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                            }
                                        })
                                        .create();
                                noQuestionsAlertDialog.show();
                            } else {
                                createEvent(selectedQuestions);
                                Snackbar.make(view, "Event created!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            }
                        }
                    });
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, getString(R.string.event_canceled), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                returnToEvents();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            if (requestCode == GALLERY_INTENT) {
                mUriPicture = data.getData();
                mSelectedPicture = true;
                Picasso.get().load(mUriPicture).fit().centerCrop().into(mImage);
            } else {
                if (requestCode == PICK_PDF) {
                    mUriPDF = data.getData();
                    mSelectedPDF = true;
                    mLoadPdf.setText(ImageUtils.getFileName(mUriPDF, CreateEventActivity.this));
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void returnToEvents() {
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog leaveAlertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.are_you_sure))
                .setMessage(getString(R.string.are_you_sure_message))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CreateEventActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing, dismiss dialog
                    }
                })
                .create();
        leaveAlertDialog.show();
    }

    public boolean validateForm() {
        // TODO move some validations on cloud functions
        boolean valid;
        valid = (editTextIsValid(mName) && editTextIsValid(mLocation) && editTextIsValid(mStartDate) &&
                editTextIsValid(mFinishDate) && editTextIsValid(mDescription) &&
                editTextIsValid(mDeadline) && editTextIsValid(mSize));
        if (valid) {
            if (TextUtils.equals(mTypeSpinner.getSelectedItem().toString(), "Type")) {
                Toast.makeText(this, getString(R.string.select_type), Toast.LENGTH_SHORT).show();
                valid = false;
            } else if ((deadline > finishDate)) {
                Toast.makeText(this, getString(R.string.deadline_after_finish_date), Toast.LENGTH_SHORT).show();
                valid = false;
            } else if (startDate > finishDate) {
                Toast.makeText(this, getString(R.string.start_date_after_finish_date), Toast.LENGTH_SHORT).show();
                valid = false;
            }
        }
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {
        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditText.setError(getString(R.string.field_empty));
            mEditText.requestFocus();
            return false;
        } else {
            mEditText.setError(null);
        }
        return true;
    }

    private void hideKeyboardFrom(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void populateSpinnerArray() {
        typeList.add("Type");
        typeList.addAll(EventType.getAllAsList());
    }

    private View.OnClickListener setOnClickListenerCalendar(final EditText editText) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        month++;
                        editText.setText(dayOfMonth + "/" + month + "/" + year);
                        month--;
                        myCalendar.set(year, month, dayOfMonth, 12, 15, 0);
                        if (editText.equals(mStartDate)) {
                            startDate = myCalendar.getTimeInMillis();
                        } else if (editText.equals(mFinishDate)) {
                            finishDate = myCalendar.getTimeInMillis();
                        } else if (editText.equals(mDeadline)) {
                            deadline = myCalendar.getTimeInMillis();
                        }

                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        };
    }

    private Uri parseUri(int ID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(ID)
                + '/' + getResources().getResourceTypeName(ID) + '/' + getResources().getResourceEntryName(ID));

    }

    private void populateUriList() {
        imageUris.add(parseUri(R.drawable.image_no_type));
        imageUris.add(parseUri(R.drawable.image_sports));
        imageUris.add(parseUri(R.drawable.image_music));
        imageUris.add(parseUri(R.drawable.image_festival));
        imageUris.add(parseUri(R.drawable.image_charity));
        imageUris.add(parseUri(R.drawable.image_training));
        imageUris.add(parseUri(R.drawable.image_other));
    }

    private void animateQuestions() {
        createEventScrollView.animate()
                .alpha(0f)
                .setDuration(longAnimTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        createEventScrollView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });

        questionText.setAlpha(0f);
        questionText.setVisibility(View.VISIBLE);
        questionText.animate()
                .alpha(1f)
                .setDuration(longAnimTime)
                .setListener(null);

        questionsScrollView.setAlpha(0f);
        questionsScrollView.setVisibility(View.VISIBLE);
        questionsScrollView.animate()
                .alpha(1f)
                .setDuration(longAnimTime)
                .setListener(null);
    }

    private void createEvent(ArrayList<String> requiredQuestions) {

        String name = mName.getText().toString();
        String location = mLocation.getText().toString();
        String description = mDescription.getText().toString();
        int size = Integer.parseInt(mSize.getText().toString());
        final String eventID = mDatabase.child("events").push().getKey();
        String type = mTypeSpinner.getSelectedItem().toString();
        StorageReference filePath = mStorage.child("Photos").child("Event").child(eventID);

        if (mSelectedPicture) {
            filePath.putBytes(ImageUtils.compressImage(mUriPicture, CreateEventActivity.this, getResources()));
        }
        if (mSelectedPDF) {
            filePath = mStorage.child("Contracts").child("Event").child(eventID);
            filePath.putFile(mUriPDF);
        }

        mDatabase.child("users/organisers/" + mUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int lastEvent = (int) dataSnapshot.child("events").getChildrenCount();
                        DataSnapshot eventsNumber = dataSnapshot.child("eventsnumber");
                        long eventsNr = eventsNumber.getValue() != null ? (Long) eventsNumber.getValue() : 0;
                        DatabaseUtils.writeData("users/organisers/" + mUser.getUid() + "/events/" + lastEvent, eventID);
                        DatabaseUtils.writeData("users/organisers/" + mUser.getUid() + "/eventsnumber", eventsNr + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // do nothing for now
                    }
                });


        Event new_event = new Event(mUser.getUid(), name, location, startDate, finishDate, type, eventID, description, deadline, size, requiredQuestions);
        DatabaseUtils.writeData("events/" + eventID, new_event);
        DatabaseUtils.writeData("events/" + eventID + "/validity", VolteemConstants.FLAG_EVENT_VALID);

        Intent alarm = new Intent(CreateEventActivity.this, NotificationEventReceiver.class);
        alarm.putExtra("nameEvent", name);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(CreateEventActivity.this, 100, alarm, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, finishDate, pendingIntent);
        } else {
            Log.w(TAG, "AlarmManager is not available");
        }



        returnToEvents();
    }
}
