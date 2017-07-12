package com.volunteer.thc.volunteerapp.presentation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;

/**
 * Created on 6/23/2017.
 */

public class CreateEventFragment extends Fragment {

    private EditText mName, mLocation, mDate, mType, mDescription, mDeadline, mSize;
    private Button mSaveEvent, mCancel;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_createevent, container, false);

        mName = (EditText) view.findViewById(R.id.event_name);
        mLocation = (EditText) view.findViewById(R.id.event_location);
        mDate = (EditText) view.findViewById(R.id.event_date);
        mType = (EditText) view.findViewById(R.id.event_type);
        mDescription = (EditText) view.findViewById(R.id.event_description);
        mDeadline = (EditText) view.findViewById(R.id.event_deadline);
        mSize = (EditText) view.findViewById(R.id.event_size);
        mSaveEvent = (Button) view.findViewById(R.id.save_event);
        mCancel = (Button) view.findViewById(R.id.cancel_event);

        mSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validateForm()) {

                    String name = mName.getText().toString();
                    String location = mLocation.getText().toString();
                    String date = mDate.getText().toString();
                    String type = mType.getText().toString();
                    String description = mDescription.getText().toString();
                    String deadline = mDeadline.getText().toString();
                    int size = Integer.parseInt(mSize.getText().toString());

                    Event new_event = new Event(user.getUid(), name, location, date, type, description,
                            deadline, size);

                    String eventID = mDatabase.child("events").push().getKey();
                    mDatabase.child("events").child(eventID).setValue(new_event);
                    SharedPreferences prefs = getActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
                    mDatabase.child("users").child("organisers").child(user.getUid()).child("events")
                            .child(prefs.getInt("lastID", 0)+"").setValue(eventID);

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
        valid = (editTextIsValid(mName) && editTextIsValid(mLocation) && editTextIsValid(mDate) && editTextIsValid(mType) &&
                editTextIsValid(mDescription) && editTextIsValid(mDeadline) && editTextIsValid(mSize));
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {

        String text = mEditText.getText().toString();
        if(TextUtils.isEmpty(text)) {
            mEditText.setError("This field can not be empty.");
            mEditText.requestFocus();
            return false;
        } else {
            mEditText.setError(null);
        }
        return true;
    }
}
