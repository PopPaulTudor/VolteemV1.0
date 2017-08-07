package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
    private MenuItem mEdit, mSave, mCancel;

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

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile_edit, menu);
        mEdit = menu.findItem(R.id.action_edit);
        mSave = menu.findItem(R.id.action_save);
        mCancel = menu.findItem(R.id.action_cancel);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_edit:
                onEditItemPressed();
                return true;
            case R.id.action_save:
                onSaveItemPressed();
                return true;
            case R.id.action_cancel:
                onCancelItemPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onEditItemPressed() {
        toggleEditOn();
        toggleFocusOn();
        mEdit.setVisible(false);
        mSave.setVisible(true);
        mCancel.setVisible(true);
    }

    private void onSaveItemPressed() {
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

            hideKeyboardFrom(getActivity(), getView());
            mEdit.setVisible(true);
            mSave.setVisible(false);
            mCancel.setVisible(false);
            toggleEditOff();
            toggleFocusOff();
        }
    }

    private void onCancelItemPressed() {
        mEdit.setVisible(true);
        mSave.setVisible(false);
        mCancel.setVisible(false);

        mName.setText(mCurrentEvent.getName());
        mLocation.setText(mCurrentEvent.getLocation());
        mDate.setText(mCurrentEvent.getDate());
        mType.setText(mCurrentEvent.getType());
        mDescription.setText(mCurrentEvent.getDescription());
        mDeadline.setText(mCurrentEvent.getDeadline());
        mSize.setText(mCurrentEvent.getSize() + "");

        mName.setError(null);
        mLocation.setError(null);
        mDate.setError(null);
        mType.setError(null);
        mDescription.setError(null);
        mDeadline.setError(null);
        mSize.setError(null);

        toggleEditOff();
        toggleFocusOff();
    }

    private void toggleEditOn() {

        mName.setKeyListener((KeyListener) mName.getTag());
        mLocation.setKeyListener((KeyListener) mLocation.getTag());
        mDate.setKeyListener((KeyListener) mDate.getTag());
        mType.setKeyListener((KeyListener) mType.getTag());
        mDescription.setKeyListener((KeyListener) mDescription.getTag());
        mDeadline.setKeyListener((KeyListener) mDeadline.getTag());
        mSize.setKeyListener((KeyListener) mSize.getTag());
    }

    private void toggleEditOff() {

        mName.setKeyListener(null);
        mLocation.setKeyListener(null);
        mDate.setKeyListener(null);
        mType.setKeyListener(null);
        mDescription.setKeyListener(null);
        mDeadline.setKeyListener(null);
        mSize.setKeyListener(null);
    }

    private void toggleFocusOn() {

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

    private void toggleFocusOff() {

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

    private boolean validateForm() {

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

    private void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
