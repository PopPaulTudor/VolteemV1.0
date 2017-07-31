package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventInfoFragment extends Fragment {

    private Event mCurrentEvent = new Event();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private EditText mName, mLocation, mDate, mType, mDescription, mDeadline, mSize;
    private Button mEditEvent, mCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiser_single_event_info, container, false);

        mCurrentEvent = (Event) getArguments().getSerializable("currentEvent");

        mName = (EditText) view.findViewById(R.id.event_name);
        mLocation = (EditText) view.findViewById(R.id.event_location);
        mDate = (EditText) view.findViewById(R.id.event_date);
        mType = (EditText) view.findViewById(R.id.event_type);
        mDescription = (EditText) view.findViewById(R.id.event_description);
        mDeadline = (EditText) view.findViewById(R.id.event_deadline);
        mSize = (EditText) view.findViewById(R.id.event_size);
        mEditEvent = (Button) view.findViewById(R.id.edit_event);
        mCancel = (Button) view.findViewById(R.id.cancel_event);

        mName.setText(mCurrentEvent.getName());
        mLocation.setText(mCurrentEvent.getLocation());
        mDate.setText(mCurrentEvent.getDate());
        mType.setText(mCurrentEvent.getType());
        mDescription.setText(mCurrentEvent.getDescription());
        mDeadline.setText(mCurrentEvent.getDeadline());
        mSize.setText(mCurrentEvent.getSize() + "");

        mName.setTag(mName.getKeyListener());
        mLocation.setTag(mLocation.getKeyListener());
        mDate.setTag(mDate.getKeyListener());
        mType.setTag(mType.getKeyListener());
        mDescription.setTag(mDescription.getKeyListener());
        mDeadline.setTag(mDeadline.getKeyListener());
        mSize.setTag(mSize.getKeyListener());

        toggleEditOff();
        toggleFocusOff();

        mEditEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCancel.getVisibility() == View.GONE) {

                    mCancel.setVisibility(View.VISIBLE);
                    mEditEvent.setText("SAVE");

                    toggleFocusOn();
                    toggleEditOn();

                } else {

                    String currentName, currentLocation, currentDate, currentType, currentDescription, currentDeadline, currentSize;

                    currentName = mName.getText().toString();
                    currentLocation = mLocation.getText().toString();
                    currentDate = mDate.getText().toString();
                    currentType = mType.getText().toString();
                    currentDescription = mDescription.getText().toString();
                    currentDeadline = mDeadline.getText().toString();
                    currentSize = mSize.getText().toString();

                    if (validateForm()) {
                        if (!currentName.equals(mCurrentEvent.getName())) {
                            mDatabase.child("events").child(mCurrentEvent.getEventID()).child("name").setValue(currentName);
                            mCurrentEvent.setName(currentName);
                        }

                        if (!currentLocation.equals(mCurrentEvent.getLocation())) {
                            mDatabase.child("events").child(mCurrentEvent.getEventID()).child("location").setValue(currentLocation);
                            mCurrentEvent.setLocation(currentLocation);
                        }

                        if (!currentDate.equals(mCurrentEvent.getDate())) {
                            mDatabase.child("events").child(mCurrentEvent.getEventID()).child("date").setValue(currentDate);
                            mCurrentEvent.setDate(currentDate);
                        }

                        if (!currentType.equals(mCurrentEvent.getType())) {
                            mDatabase.child("events").child(mCurrentEvent.getEventID()).child("type").setValue(currentType);
                            mCurrentEvent.setType(currentType);
                        }

                        if (!currentDescription.equals(mCurrentEvent.getDescription())) {
                            mDatabase.child("events").child(mCurrentEvent.getEventID()).child("description").setValue(currentDescription);
                            mCurrentEvent.setDescription(currentDescription);
                        }

                        if (!currentDeadline.equals(mCurrentEvent.getDeadline())) {
                            mDatabase.child("events").child(mCurrentEvent.getEventID()).child("deadline").setValue(currentDescription);
                            mCurrentEvent.setDeadline(currentDeadline);
                        }

                        if (!currentSize.equals(mCurrentEvent.getSize())) {
                            mDatabase.child("events").child(mCurrentEvent.getEventID()).child("size").setValue(Integer.parseInt(currentSize));
                            mCurrentEvent.setSize(Integer.parseInt(currentSize));
                        }

                        Toast.makeText(getActivity(), "Event updated!", Toast.LENGTH_LONG).show();

                        mCancel.setVisibility(View.GONE);
                        mEditEvent.setText("EDIT EVENT");
                        toggleEditOff();
                        toggleFocusOff();
                    }
                }
            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mName.setText(mCurrentEvent.getName());
                mLocation.setText(mCurrentEvent.getLocation());
                mDate.setText(mCurrentEvent.getDate());
                mType.setText(mCurrentEvent.getType());
                mDescription.setText(mCurrentEvent.getDescription());
                mDeadline.setText(mCurrentEvent.getDeadline());
                mSize.setText(mCurrentEvent.getSize() + "");

                toggleEditOff();
                toggleFocusOff();

                mEditEvent.setText("EDIT");
                mCancel.setVisibility(View.GONE);
            }
        });

        return view;
    }

    public void toggleEditOn() {

        mName.setKeyListener((KeyListener) mName.getTag());
        mLocation.setKeyListener((KeyListener) mLocation.getTag());
        mDate.setKeyListener((KeyListener) mDate.getTag());
        mType.setKeyListener((KeyListener) mType.getTag());
        mDescription.setKeyListener((KeyListener) mDescription.getTag());
        mDeadline.setKeyListener((KeyListener) mDeadline.getTag());
        mSize.setKeyListener((KeyListener) mSize.getTag());
    }

    public void toggleEditOff() {

        mName.setKeyListener(null);
        mLocation.setKeyListener(null);
        mDate.setKeyListener(null);
        mType.setKeyListener(null);
        mDescription.setKeyListener(null);
        mDeadline.setKeyListener(null);
        mSize.setKeyListener(null);
    }

    public void toggleFocusOn() {

        mName.setFocusableInTouchMode(true);
        mName.setFocusable(true);
        mLocation.setFocusableInTouchMode(true);
        mLocation.setFocusable(true);
        mDate.setFocusableInTouchMode(true);
        mDate.setFocusable(true);
        mType.setFocusableInTouchMode(true);
        mType.setFocusable(true);
        mDescription.setFocusableInTouchMode(true);
        mDescription.setFocusable(true);
        mDeadline.setFocusableInTouchMode(true);
        mDeadline.setFocusable(true);
        mSize.setFocusableInTouchMode(true);
        mSize.setFocusable(true);
    }

    public void toggleFocusOff() {

        mName.setFocusableInTouchMode(false);
        mName.setFocusable(false);
        mLocation.setFocusableInTouchMode(false);
        mLocation.setFocusable(false);
        mDate.setFocusableInTouchMode(false);
        mDate.setFocusable(false);
        mType.setFocusableInTouchMode(false);
        mType.setFocusable(false);
        mDescription.setFocusableInTouchMode(false);
        mDescription.setFocusable(false);
        mDeadline.setFocusableInTouchMode(false);
        mDeadline.setFocusable(false);
        mSize.setFocusableInTouchMode(false);
        mSize.setFocusable(false);
    }

    public boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mName) && editTextIsValid(mLocation) && editTextIsValid(mDate) &&
                editTextIsValid(mType) && editTextIsValid(mDescription) && editTextIsValid(mDeadline) && editTextIsValid(mSize));
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
}
