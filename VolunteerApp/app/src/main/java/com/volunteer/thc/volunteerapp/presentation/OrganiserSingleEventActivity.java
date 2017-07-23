package com.volunteer.thc.volunteerapp.presentation;

import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Event;

/**
 * Created by poppa on 13.07.2017.
 */

public class OrganiserSingleEventActivity extends AppCompatActivity {

    private Event mCurrentEvent;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private EditText mName, mLocation, mDate, mType, mDescription, mDeadline, mSize;
    private Button mEditEvent, mCancel, mShowUsers;
    private ListView mRegisteredUsers;
    private ScrollView mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organisersingleevent);

        mCurrentEvent = (Event) getIntent().getSerializableExtra("SingleEvent");

        mName = (EditText) findViewById(R.id.event_name);
        mLocation = (EditText) findViewById(R.id.event_location);
        mDate = (EditText) findViewById(R.id.event_date);
        mType = (EditText) findViewById(R.id.event_type);
        mDescription = (EditText) findViewById(R.id.event_description);
        mDeadline = (EditText) findViewById(R.id.event_deadline);
        mSize = (EditText) findViewById(R.id.event_size);
        mEditEvent = (Button) findViewById(R.id.edit_event);
        mShowUsers = (Button) findViewById(R.id.show_users);
        mCancel = (Button) findViewById(R.id.cancel_event);
        mRegisteredUsers = (ListView) findViewById(R.id.registered_users);
        mEvent = (ScrollView) findViewById(R.id.create_event);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mCurrentEvent.getRegistered_volunteers());
        mRegisteredUsers.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

        mName.setText(mCurrentEvent.getName());
        mLocation.setText(mCurrentEvent.getLocation());
        mDate.setText(mCurrentEvent.getDate());
        mType.setText(mCurrentEvent.getType());
        mDescription.setText(mCurrentEvent.getDescription());
        mDeadline.setText(mCurrentEvent.getDeadline());
        mSize.setText(mCurrentEvent.getSize()+"");

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

                if(mCancel.getVisibility() == View.GONE){

                    mCancel.setVisibility(View.VISIBLE);
                    mEditEvent.setText("SAVE");
                    mShowUsers.setVisibility(View.GONE);

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

                    if(validateForm()) {
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

                        Toast.makeText(OrganiserSingleEventActivity.this, "Event updated!", Toast.LENGTH_LONG).show();

                        mCancel.setVisibility(View.GONE);
                        mShowUsers.setVisibility(View.VISIBLE);
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
                mSize.setText(mCurrentEvent.getSize()+"");

                toggleEditOff();
                toggleFocusOff();

                mEditEvent.setText("EDIT");
                mShowUsers.setVisibility(View.VISIBLE);
                mCancel.setVisibility(View.GONE);
            }
        });

        mShowUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mName.getVisibility() == View.VISIBLE) {

                    mShowUsers.setText("HIDE REGISTERED USERS");
                    mRegisteredUsers.setVisibility(View.VISIBLE);
                    mEvent.setVisibility(View.GONE);
                } else {
                    mShowUsers.setText("SHOW REGISTERED USERS");
                    mRegisteredUsers.setVisibility(View.GONE);
                    mEvent.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void toggleEditOn(){

        mName.setKeyListener((KeyListener) mName.getTag());
        mLocation.setKeyListener((KeyListener) mLocation.getTag());
        mDate.setKeyListener((KeyListener) mDate.getTag());
        mType.setKeyListener((KeyListener) mType.getTag());
        mDescription.setKeyListener((KeyListener) mDescription.getTag());
        mDeadline.setKeyListener((KeyListener) mDeadline.getTag());
        mSize.setKeyListener((KeyListener) mSize.getTag());
    }

    public void toggleEditOff(){

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
