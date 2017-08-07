package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.Util.CalendarUtil;
import com.volunteer.thc.volunteerapp.model.Event;

import java.util.Calendar;

/**
 * Created by Cristi on 7/27/2017.
 */

public class OrganiserSingleEventInfoFragment extends Fragment {

    private Event mCurrentEvent = new Event();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private EditText mName, mLocation, mStartDate, mType, mDescription, mDeadline, mSize, mFinishDate;
    private long field = 0;
    private MenuItem mEdit, mSave, mCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiser_single_event_info, container, false);

        mCurrentEvent = (Event) getArguments().getSerializable("currentEvent");

        mName = (EditText) view.findViewById(R.id.event_name);
        mLocation = (EditText) view.findViewById(R.id.event_location);
        mStartDate = (EditText) view.findViewById(R.id.event_date_start);
        mFinishDate = (EditText) view.findViewById(R.id.event_date_finish);
        mDeadline = (EditText) view.findViewById(R.id.event_deadline);
        mType = (EditText) view.findViewById(R.id.event_type);
        mDescription = (EditText) view.findViewById(R.id.event_description);
        mSize = (EditText) view.findViewById(R.id.event_size);

        mName.setText(mCurrentEvent.getName());
        mLocation.setText(mCurrentEvent.getLocation());
        mType.setText(mCurrentEvent.getType());
        mDescription.setText(mCurrentEvent.getDescription());
        mDeadline.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getDeadline()));
        mStartDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getStartDate()));
        mFinishDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getFinishDate()));

        mSize.setText(mCurrentEvent.getSize() + "");

        mName.setTag(mName.getKeyListener());
        mLocation.setTag(mLocation.getKeyListener());
        mStartDate.setTag(mStartDate.getKeyListener());
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
        toggleFocusOn();
        toggleEditOn();
        mEdit.setVisible(false);
        mSave.setVisible(true);
        mCancel.setVisible(true);
    }

    private void onSaveItemPressed() {
        String currentName, currentLocation, currentType, currentDescription, currentSize;
        long currentStartDate, currentFinishDate, currentDeadline;

        currentName = mName.getText().toString();
        currentLocation = mLocation.getText().toString();
        currentType = mType.getText().toString();
        currentDescription = mDescription.getText().toString();
        currentSize = mSize.getText().toString();

        mStartDate.setOnClickListener(setonClickListenerCalendar());
        currentStartDate = field;
        mFinishDate.setOnClickListener(setonClickListenerCalendar());
        currentFinishDate = field;
        mDeadline.setOnClickListener(setonClickListenerCalendar());
        currentDeadline = field;

        if (validateForm()) {
            if (!currentName.equals(mCurrentEvent.getName())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("name").setValue(currentName);
                mCurrentEvent.setName(currentName);
            }

            if (!currentLocation.equals(mCurrentEvent.getLocation())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("location").setValue(currentLocation);
                mCurrentEvent.setLocation(currentLocation);
            }

            if (currentStartDate != mCurrentEvent.getStartDate()) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("date").setValue(currentStartDate);
                mCurrentEvent.setStartDate(currentStartDate);
            }
            if (currentFinishDate != mCurrentEvent.getFinishDate()) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("date").setValue(currentFinishDate);
                mCurrentEvent.setStartDate(currentFinishDate);
            }

            if (!currentType.equals(mCurrentEvent.getType())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("type").setValue(currentType);
                mCurrentEvent.setType(currentType);
            }

            if (!currentDescription.equals(mCurrentEvent.getDescription())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("description").setValue(currentDescription);
                mCurrentEvent.setDescription(currentDescription);
            }

            if (currentDeadline != mCurrentEvent.getDeadline()) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("deadline").setValue(currentDeadline);
                mCurrentEvent.setDeadline(currentDeadline);
            }

            if (!currentSize.equals(mCurrentEvent.getSize())) {
                mDatabase.child("events").child(mCurrentEvent.getEventID()).child("size").setValue(Integer.parseInt(currentSize));
                mCurrentEvent.setSize(Integer.parseInt(currentSize));
            }

            Toast.makeText(getActivity(), "Event updated!", Toast.LENGTH_LONG).show();

            mEdit.setVisible(true);
            mSave.setVisible(false);
            mCancel.setVisible(false);
            toggleEditOff();
            toggleFocusOff();
            hideKeyboardFrom(getActivity(),getView());
        }
    }

    private void onCancelItemPressed() {

        mName.setText(mCurrentEvent.getName());
        mLocation.setText(mCurrentEvent.getLocation());
        mType.setText(mCurrentEvent.getType());
        mDescription.setText(mCurrentEvent.getDescription());
        mSize.setText(mCurrentEvent.getSize() + "");
        mDeadline.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getDeadline()));
        mStartDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getStartDate()));
        mFinishDate.setText(CalendarUtil.getStringDateFromMM(mCurrentEvent.getFinishDate()));

        mName.setError(null);
        mLocation.setError(null);
        mType.setError(null);
        mDescription.setError(null);
        mDeadline.setError(null);
        mStartDate.setError(null);
        mSize.setError(null);
        mFinishDate.setError(null);

        toggleEditOff();
        toggleFocusOff();
        mEdit.setVisible(true);
        mCancel.setVisible(false);
        mSave.setVisible(false);
        hideKeyboardFrom(getActivity(),getView());
    }

    public void toggleEditOn() {

        mName.setKeyListener((KeyListener) mName.getTag());
        mLocation.setKeyListener((KeyListener) mLocation.getTag());
        mStartDate.setKeyListener((KeyListener) mStartDate.getTag());
        mFinishDate.setKeyListener((KeyListener) mFinishDate.getTag());
        mType.setKeyListener((KeyListener) mType.getTag());
        mDescription.setKeyListener((KeyListener) mDescription.getTag());
        mDeadline.setKeyListener((KeyListener) mDeadline.getTag());
        mSize.setKeyListener((KeyListener) mSize.getTag());
    }

    public void toggleEditOff() {

        mName.setKeyListener(null);
        mLocation.setKeyListener(null);
        mStartDate.setKeyListener(null);
        mFinishDate.setKeyListener(null);
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
        mStartDate.setFocusableInTouchMode(true);
        mStartDate.setFocusable(true);
        mFinishDate.setFocusableInTouchMode(true);
        mFinishDate.setFocusable(true);
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
        mStartDate.setFocusableInTouchMode(false);
        mStartDate.setFocusable(false);
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

    private void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    View.OnClickListener setonClickListenerCalendar() {
        final Calendar myCalendar = Calendar.getInstance();
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        month++;
                        mStartDate.setText(dayOfMonth + "/" + month + "/" + year);
                        month--;
                        myCalendar.set(year, month, dayOfMonth);
                        field = myCalendar.getTimeInMillis();

                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        };
    }
}
