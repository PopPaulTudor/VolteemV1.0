package com.volunteer.thc.volunteerapp.presentation;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserEventsFragment;
import com.volunteer.thc.volunteerapp.util.DatabaseUtils;
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created on 6/23/2017.
 */

public class CreateEventFragment extends Fragment {

    private static final int GALLERY_INTENT = 1;
    private EditText mName, mLocation, mDescription, mDeadline, mSize, mStartDate, mFinishDate;
    private ImageView mImage;
    private Spinner mType;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private long startDate = -1, finishDate, deadline;
    private StorageReference mStorage;
    private Uri uri = null;
    private ArrayList<String> typeList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_createevent, container, false);

        mName = (EditText) view.findViewById(R.id.event_name);
        mLocation = (EditText) view.findViewById(R.id.event_location);
        mStartDate = (EditText) view.findViewById(R.id.event_date_start_create);
        mFinishDate = (EditText) view.findViewById(R.id.event_date_finish_create);
        mType = (Spinner) view.findViewById(R.id.event_type);
        mDescription = (EditText) view.findViewById(R.id.event_description);
        mDeadline = (EditText) view.findViewById(R.id.event_deadline_create);
        mImage = (ImageView) view.findViewById(R.id.event_image);
        mSize = (EditText) view.findViewById(R.id.event_size);
        mStorage = FirebaseStorage.getInstance().getReference();

        Button mSaveEvent = (Button) view.findViewById(R.id.save_event);
        Button mCancel = (Button) view.findViewById(R.id.cancel_event);

        populateSpinnerArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, typeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mType.setAdapter(adapter);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (PermissionUtil.isStoragePermissionGranted(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);

                } else {
                    Snackbar.make(view, "Please allow storage permission", Snackbar.LENGTH_LONG).setAction("Set Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    }).show();
                }
            }
        });

        mStartDate.setOnClickListener(setonClickListenerCalendar(mStartDate));
        mFinishDate.setOnClickListener(setonClickListenerCalendar(mFinishDate));
        mDeadline.setOnClickListener(setonClickListenerCalendar(mDeadline));

        mSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateForm()) {

                    hideKeyboardFrom(getActivity(), getView());
                    String name = mName.getText().toString();
                    String location = mLocation.getText().toString();
                    String description = mDescription.getText().toString();
                    int size = Integer.parseInt(mSize.getText().toString());
                    final String eventID = mDatabase.child("events").push().getKey();
                    String type = mType.getSelectedItem().toString();

                    StorageReference filePath = mStorage.child("Photos").child("Event").child(eventID);
                    filePath.putBytes(ImageUtils.compressImage(uri, getActivity()));

                    mDatabase.child("users/organisers/" + user.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int lastEvent = (int) dataSnapshot.child("events").getChildrenCount();
                                    int eventsNr = dataSnapshot.child("eventsnumber").getValue(Integer.class);
                                    DatabaseUtils.writeData("users/organisers/" + user.getUid() + "/events/" + lastEvent, eventID);
                                    DatabaseUtils.writeData("users/organisers/" + user.getUid() + "/eventsnumber", eventsNr + 1);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    Event new_event = new Event(user.getUid(), name, location, startDate, finishDate, type, eventID, description, deadline, size);
                    DatabaseUtils.writeData("events/" + eventID, new_event);
                    DatabaseUtils.writeData("events/" + eventID + "/validity", "valid");

                    returnToEvents();
                    Snackbar.make(view, "Event created!", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Event canceled.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                returnToEvents();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && (data != null)) {
            uri = data.getData();
            Picasso.with(getContext()).load(uri).fit().centerCrop().into(mImage);
        }
    }

    private void returnToEvents() {

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, new OrganiserEventsFragment());
        fragmentTransaction.commit();
    }

    public boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mName) && editTextIsValid(mLocation) && editTextIsValid(mStartDate) &&
                editTextIsValid(mFinishDate) && editTextIsValid(mDescription) &&
                editTextIsValid(mDeadline) && editTextIsValid(mSize) && (uri != null));
        if(valid && TextUtils.equals(mType.getSelectedItem().toString(), "Type")) {
            Toast.makeText(getActivity(), "Please select a type.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (valid && (deadline > finishDate)) {
            Toast.makeText(getActivity(), "The deadline can not be after the finish date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (valid && (startDate > finishDate)) {
            Toast.makeText(getActivity(), "The start date can not be after the finish date.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {
        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditText.setError("This field can not be empty.");
            mEditText.requestFocus();
            return false;
        } else {
            mEditText.setError(null);
        }
        return true;
    }

    private void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void populateSpinnerArray() {
        typeList.add("Type");
        typeList.add("Sports");
        typeList.add("Music");
        typeList.add("Festival");
        typeList.add("Charity");
        typeList.add("Training");
        typeList.add("Other");
    }

    View.OnClickListener setonClickListenerCalendar(final EditText editText) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        month++;
                        editText.setText(dayOfMonth + "/" + month + "/" + year);
                        month--;
                        myCalendar.set(year, month, dayOfMonth, 0, 0, 0);
                        if (editText.equals(mStartDate)) startDate = myCalendar.getTimeInMillis();
                        else if (editText.equals(mFinishDate))
                            finishDate = myCalendar.getTimeInMillis();
                        else if (editText.equals(mDeadline))
                            deadline = myCalendar.getTimeInMillis();

                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        };
    }
}
