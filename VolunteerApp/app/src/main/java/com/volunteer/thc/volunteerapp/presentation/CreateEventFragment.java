package com.volunteer.thc.volunteerapp.presentation;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;
import com.volunteer.thc.volunteerapp.presentation.organiser.OrganiserEventsFragment;

import java.util.Calendar;

;

/**
 * Created on 6/23/2017.
 */

public class CreateEventFragment extends Fragment {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_OK = 1;
    private EditText mName, mLocation, mType, mDescription, mDeadline, mSize, mStartDate, mFinishDate;
    private ImageView mImage;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference mStorageRef;
    private long startDate=-1, finishDate, deadline;
    final Calendar myCalendar = Calendar.getInstance();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_createevent, container, false);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mName = (EditText) view.findViewById(R.id.event_name);
        mLocation = (EditText) view.findViewById(R.id.event_location);
        mStartDate = (EditText) view.findViewById(R.id.event_date_start_create);
        mFinishDate = (EditText) view.findViewById(R.id.event_date_finish_create);
        mType = (EditText) view.findViewById(R.id.event_type);
        mDescription = (EditText) view.findViewById(R.id.event_description);
        mDeadline = (EditText) view.findViewById(R.id.event_deadline_create);
        mSize = (EditText) view.findViewById(R.id.event_size);

        Button mSaveEvent = (Button) view.findViewById(R.id.save_event);
        Button mCancel = (Button) view.findViewById(R.id.cancel_event);
        Button chooseImage = (Button) view.findViewById(R.id.event_add_image);


        /*
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                mImage= (ImageView) getView().findViewById(R.id.event_image);
                mImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));

            }

        }*/


        mStartDate.setOnClickListener(setonClickListenerCalendar(mStartDate));
        mFinishDate.setOnClickListener(setonClickListenerCalendar(mFinishDate));
        mDeadline.setOnClickListener(setonClickListenerCalendar(mDeadline));


        mSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validateForm()) {

                    String name = mName.getText().toString();
                    String location = mLocation.getText().toString();
                    String type = mType.getText().toString();
                    String description = mDescription.getText().toString();
                    int size = Integer.parseInt(mSize.getText().toString());
                    String eventID = mDatabase.child("events").push().getKey();


                    Event new_event = new Event(user.getUid(), name, location, startDate, finishDate, type, eventID, description, deadline, size);
                    mDatabase.child("events").child(eventID).setValue(new_event);
                    SharedPreferences prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    mDatabase.child("users").child("organisers").child(user.getUid()).child("events")
                            .child(prefs.getInt("lastID", 0) + "").setValue(eventID);

                    ///TODO: increase number of organiser's events

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

    private void returnToEvents() {

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, new OrganiserEventsFragment());
        fragmentTransaction.commit();
    }

    public boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mName) && editTextIsValid(mLocation) && editTextIsValid(mType) &&
                editTextIsValid(mDescription) && editTextIsValid(mDeadline) && editTextIsValid(mSize));
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


    View.OnClickListener setonClickListenerCalendar(final TextView textView) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {


                        month++;
                        textView.setText(dayOfMonth + "/" + month + "/" + year);
                        month--;
                        myCalendar.set(year, month, dayOfMonth);
                        if (textView.equals(mStartDate)) startDate = myCalendar.getTimeInMillis();
                        else if (textView.equals(mFinishDate))
                            finishDate = myCalendar.getTimeInMillis();
                        else if (textView.equals(mDeadline))
                            deadline = myCalendar.getTimeInMillis();


                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        };
    }

}
