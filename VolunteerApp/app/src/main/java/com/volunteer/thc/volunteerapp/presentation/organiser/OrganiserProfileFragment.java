package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Organiser;

/**
 * Created by Cristi on 6/19/2017.
 */

public class OrganiserProfileFragment extends Fragment {

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private EditText mEmail, mCompany, mPhone, mCity;
    private ProgressBar mProgressBar;
    private Organiser organiser;
    private MenuItem mSave, mEdit, mCancel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserprofile, container, false);

        organiser = new Organiser();
        mEmail = (EditText) view.findViewById(R.id.edit_email);
        mCompany = (EditText) view.findViewById(R.id.edit_company);
        mCity = (EditText) view.findViewById(R.id.edit_city);
        mPhone = (EditText) view.findViewById(R.id.edit_phone);

        mCompany.setTag(mCompany.getKeyListener());
        mEmail.setTag(mEmail.getKeyListener());
        mCity.setTag(mCity.getKeyListener());
        mPhone.setTag(mPhone.getKeyListener());

        mEmail.setKeyListener(null);

        mProgressBar = (ProgressBar) view.findViewById(R.id.indeterminateBar);
        mProgressBar.setVisibility(View.VISIBLE);

        toggleEditOff();

        ValueEventListener mOrganiserProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                organiser = dataSnapshot.getValue(Organiser.class);

                mCompany.setText(organiser.getCompany());
                mEmail.setText(organiser.getEmail());
                mPhone.setText(organiser.getPhone());
                mCity.setText(organiser.getCity());

                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ProfileReadCanceled: ", databaseError.toException());
            }
        };

        mDatabase.child("users").child("organisers").child(user.getUid()).addListenerForSingleValueEvent(mOrganiserProfileListener);
        mDatabase.removeEventListener(mOrganiserProfileListener);

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
                mEdit.setVisible(false);
                mSave.setVisible(true);
                mCancel.setVisible(true);
                return true;
            case R.id.action_save:
                onSaveItemPressed();
                mEdit.setVisible(true);
                mSave.setVisible(false);
                mCancel.setVisible(false);
                return true;
            case R.id.action_cancel:
                onCancelItemPressed();
                mEdit.setVisible(true);
                mSave.setVisible(false);
                mCancel.setVisible(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onEditItemPressed() {
        toggleFocusOn();
        toggleEditOn();
    }

    private void onSaveItemPressed() {
        String currentCompany, currentCity, currentPhone;

        currentCompany = mCompany.getText().toString();
        currentCity = mCity.getText().toString();
        currentPhone = mPhone.getText().toString();

        if (validateForm()) {
            if (!currentCompany.equals(organiser.getCompany())) {
                mDatabase.child("users").child("organisers").child(user.getUid()).child("company").setValue(currentCompany);
                organiser.setCompany(currentCompany);
            }

            if (!currentCity.equals(organiser.getCity())) {
                mDatabase.child("users").child("organisers").child(user.getUid()).child("city").setValue(currentCity);
                organiser.setCity(currentCity);
            }

            if (!currentPhone.equals(organiser.getPhone())) {
                mDatabase.child("users").child("organisers").child(user.getUid()).child("phone").setValue(currentPhone);
                organiser.setPhone(currentPhone);
            }

            Toast.makeText(getActivity(), "Changes saved!", Toast.LENGTH_SHORT).show();
            toggleEditOff();
            toggleFocusOff();
        }
    }

    private void onCancelItemPressed() {
        mCompany.setText(organiser.getCompany());
        mEmail.setText(organiser.getEmail());
        mPhone.setText(organiser.getPhone());
        mCity.setText(organiser.getCity());

        toggleEditOff();
        toggleFocusOff();
    }

    private void toggleEditOn() {

        mCompany.setKeyListener((KeyListener) mCompany.getTag());
        mPhone.setKeyListener((KeyListener) mPhone.getTag());
        mCity.setKeyListener((KeyListener) mCity.getTag());
    }

    private void toggleEditOff() {

        mCompany.setKeyListener(null);
        mPhone.setKeyListener(null);
        mCity.setKeyListener(null);
    }

    private void toggleFocusOn() {

        mEmail.setFocusableInTouchMode(true);
        mEmail.setFocusable(true);
        mCompany.setFocusableInTouchMode(true);
        mCompany.setFocusable(true);
        mPhone.setFocusableInTouchMode(true);
        mPhone.setFocusable(true);
        mCity.setFocusableInTouchMode(true);
        mCity.setFocusable(true);
    }

    private void toggleFocusOff() {

        mEmail.setFocusableInTouchMode(false);
        mEmail.setFocusable(false);
        mCompany.setFocusableInTouchMode(false);
        mCompany.setFocusable(false);
        mPhone.setFocusableInTouchMode(false);
        mPhone.setFocusable(false);
        mCity.setFocusableInTouchMode(false);
        mCity.setFocusable(false);
    }

    private boolean validateForm() {

        boolean valid = true;
        valid = (editTextIsValid(mCompany) && editTextIsValid(mCity) && editTextIsValid(mPhone));
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
