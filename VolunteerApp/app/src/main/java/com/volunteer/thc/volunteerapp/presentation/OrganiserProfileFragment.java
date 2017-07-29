package com.volunteer.thc.volunteerapp.presentation;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class OrganiserProfileFragment extends Fragment{

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private TextView mUserCompany;
    private EditText mEmail, mCompany, mPhone, mCity;
    private Button mEditSave, mCancel;
    private ProgressDialog mDialog;
    private Organiser organiser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserprofile, container, false);

        organiser = new Organiser();
        mUserCompany = (TextView) view.findViewById(R.id.user_company);
        mEmail = (EditText) view.findViewById(R.id.edit_email);
        mCompany = (EditText) view.findViewById(R.id.edit_company);
        mCity = (EditText) view.findViewById(R.id.edit_city);
        mPhone = (EditText) view.findViewById(R.id.edit_phone);
        mEditSave = (Button) view.findViewById(R.id.edit_save);
        mCancel = (Button) view.findViewById(R.id.cancel);

        mCompany.setTag(mCompany.getKeyListener());
        mEmail.setTag(mEmail.getKeyListener());
        mCity.setTag(mCity.getKeyListener());
        mPhone.setTag(mPhone.getKeyListener());

        mEmail.setKeyListener(null);

        mDialog = ProgressDialog.show(getActivity(), "Loading profile", "", false);

        toggleEditOff();

        ValueEventListener mOrganiserProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                organiser = dataSnapshot.getValue(Organiser.class);

                mUserCompany.setText("Hello " + organiser.getCompany() + "!");
                mCompany.setText(organiser.getCompany());
                mEmail.setText(organiser.getEmail());
                mPhone.setText(organiser.getPhone());
                mCity.setText(organiser.getCity());

                mDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ProfileReadCanceled: ", databaseError.toException());
            }
        };

        mDatabase.child("users").child("organisers").child(user.getUid()).addListenerForSingleValueEvent(mOrganiserProfileListener);
        mDatabase.removeEventListener(mOrganiserProfileListener);

        mEditSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                toggleFocusOn();

                if(mCancel.getVisibility() == View.GONE){

                    mCancel.setVisibility(View.VISIBLE);
                    mEditSave.setText("SAVE");

                    toggleEditOn();

                } else {

                    String currentCompany, currentCity, currentPhone;

                    currentCompany = mCompany.getText().toString();
                    currentCity = mCity.getText().toString();
                    currentPhone = mPhone.getText().toString();

                    if(validateForm()) {
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

                        mCancel.setVisibility(View.GONE);
                        mEditSave.setText("EDIT");
                        toggleEditOff();
                        toggleFocusOff();
                    }
                }

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCompany.setText(organiser.getCompany());
                mEmail.setText(organiser.getEmail());
                mPhone.setText(organiser.getPhone());
                mCity.setText(organiser.getCity());

                toggleEditOff();
                toggleFocusOff();

                mEditSave.setText("EDIT");
                mCancel.setVisibility(View.GONE);
            }
        });

        return view;
    }

    public void toggleEditOn(){

        mCompany.setKeyListener((KeyListener) mCompany.getTag());
        mPhone.setKeyListener((KeyListener) mPhone.getTag());
        mCity.setKeyListener((KeyListener) mCity.getTag());
    }

    public void toggleEditOff(){

        mCompany.setKeyListener(null);
        mPhone.setKeyListener(null);
        mCity.setKeyListener(null);
    }

    public void toggleFocusOn() {

        mEmail.setFocusableInTouchMode(true);
        mEmail.setFocusable(true);
        mCompany.setFocusableInTouchMode(true);
        mCompany.setFocusable(true);
        mPhone.setFocusableInTouchMode(true);
        mPhone.setFocusable(true);
        mCity.setFocusableInTouchMode(true);
        mCity.setFocusable(true);
    }

    public void toggleFocusOff() {

        mEmail.setFocusableInTouchMode(false);
        mEmail.setFocusable(false);
        mCompany.setFocusableInTouchMode(false);
        mCompany.setFocusable(false);
        mPhone.setFocusableInTouchMode(false);
        mPhone.setFocusable(false);
        mCity.setFocusableInTouchMode(false);
        mCity.setFocusable(false);
    }

    public boolean validateForm() {

        boolean valid = true;
        valid = (editTextIsValid(mCompany) && editTextIsValid(mCity) && editTextIsValid(mPhone));
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
