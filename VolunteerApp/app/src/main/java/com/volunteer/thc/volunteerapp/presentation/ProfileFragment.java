package com.volunteer.thc.volunteerapp.presentation;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.volunteer.thc.volunteerapp.model.User;


public class ProfileFragment extends Fragment {


    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private TextView mFirstname;
    private EditText mFirstnameEdit, mLastname, mEmail, mAge, mCity, mPhone;
    private Button mEditSave, mCancel;
    private User user1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        user1 = new User();
        mFirstname = (TextView) view.findViewById(R.id.user_firstname);
        mFirstnameEdit = (EditText) view.findViewById(R.id.edit_firstname);
        mLastname = (EditText) view.findViewById(R.id.edit_lastname);
        mEmail = (EditText) view.findViewById(R.id.edit_email);
        mAge = (EditText) view.findViewById(R.id.edit_age);
        mCity = (EditText) view.findViewById(R.id.edit_city);
        mPhone = (EditText) view.findViewById(R.id.edit_phone);

        mEditSave = (Button) view.findViewById(R.id.edit_save);
        mCancel = (Button) view.findViewById(R.id.cancel);

        mFirstnameEdit.setTag(mFirstnameEdit.getKeyListener());
        mLastname.setTag(mLastname.getKeyListener());
        mEmail.setTag(mEmail.getKeyListener());
        mAge.setTag(mAge.getKeyListener());
        mCity.setTag(mCity.getKeyListener());
        mPhone.setTag(mPhone.getKeyListener());

        mEmail.setKeyListener(null);

        toggleEditOff();

        ValueEventListener userprofileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                user1 = dataSnapshot.getValue(User.class);

                mFirstname.setText("Hello " + user1.getFirstname() + "!");
                mFirstnameEdit.setText(user1.getFirstname());
                mEmail.setText(user1.getEmail());
                mLastname.setText(user1.getLastname());
                mPhone.setText(user1.getPhone());
                mCity.setText(user1.getCity());
                mAge.setText(user1.getAge());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ProfileRead", databaseError.toException());
            }
        };

        mDatabase.child("users").child(user.getUid()).addListenerForSingleValueEvent(userprofileListener);

        mEditSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mCancel.getVisibility() == View.GONE){

                    mCancel.setVisibility(View.VISIBLE);
                    mEditSave.setText("SAVE");

                    toggleEditOn();

                } else {

                    String currentFirstName, currentLastName, currentAge, currentCity, currentPhone;

                    currentFirstName = mFirstnameEdit.getText().toString();
                    currentLastName = mLastname.getText().toString();
                    currentAge = mAge.getText().toString();
                    currentCity = mCity.getText().toString();
                    currentPhone = mPhone.getText().toString();

                    if(validateForm()) {
                        if (!currentFirstName.equals(user1.getFirstname())) {
                            mDatabase.child("users").child(user.getUid()).child("firstname").setValue(currentFirstName);
                            user1.setFirstname(currentFirstName);
                        }

                        if (!currentLastName.equals(user1.getLastname())) {
                            mDatabase.child("users").child(user.getUid()).child("lastname").setValue(currentLastName);
                            user1.setLastname(currentLastName);
                        }

                        if (!currentAge.equals(user1.getAge())) {
                            mDatabase.child("users").child(user.getUid()).child("age").setValue(currentAge);
                            user1.setAge(currentAge);
                        }

                        if (!currentCity.equals(user1.getCity())) {
                            mDatabase.child("users").child(user.getUid()).child("city").setValue(currentCity);
                            user1.setCity(currentCity);
                        }

                        if (!currentPhone.equals(user1.getPhone())) {
                            mDatabase.child("users").child(user.getUid()).child("phone").setValue(currentPhone);
                            user1.setPhone(currentPhone);
                        }

                        Toast.makeText(getActivity(), "Changes saved!", Toast.LENGTH_SHORT).show();

                        mCancel.setVisibility(View.GONE);
                        mEditSave.setText("EDIT");
                        toggleEditOff();
                    }
                }

            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirstnameEdit.setText(user1.getFirstname());
                mEmail.setText(user1.getEmail());
                mLastname.setText(user1.getLastname());
                mPhone.setText(user1.getPhone());
                mCity.setText(user1.getCity());
                mAge.setText(user1.getAge());

                mFirstnameEdit.setKeyListener(null);
                mLastname.setKeyListener(null);
                mPhone.setKeyListener(null);
                mCity.setKeyListener(null);
                mAge.setKeyListener(null);

                mEditSave.setText("EDIT");
                mCancel.setVisibility(View.GONE);
            }
        });

        return view;
    }

    public void toggleEditOn(){

        mFirstnameEdit.setKeyListener((KeyListener) mFirstnameEdit.getTag());
        mLastname.setKeyListener((KeyListener) mLastname.getTag());
        mPhone.setKeyListener((KeyListener) mPhone.getTag());
        mAge.setKeyListener((KeyListener) mAge.getTag());
        mCity.setKeyListener((KeyListener) mCity.getTag());
    }

    public void toggleEditOff(){

        mFirstnameEdit.setKeyListener(null);
        mLastname.setKeyListener(null);
        mPhone.setKeyListener(null);
        mCity.setKeyListener(null);
        mAge.setKeyListener(null);
    }

    public boolean validateForm() {

        boolean valid = true;

        String firstname = mFirstnameEdit.getText().toString();
        if (TextUtils.isEmpty(firstname)) {
            mFirstnameEdit.setError("This field can not be empty.");
            valid = false;
        } else {
            mFirstnameEdit.setError(null);
        }

        String lastname = mLastname.getText().toString();
        if (TextUtils.isEmpty(lastname)) {
            mLastname.setError("This field can not be empty.");
            valid = false;
        } else {
            mLastname.setError(null);
        }

        String age = mAge.getText().toString();
        if (TextUtils.isEmpty(age)) {
            mAge.setError("This field can not be empty.");
            valid = false;
        } else {
            mAge.setError(null);
        }
        String phone = mPhone.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            mPhone.setError("This field can not be empty.");
            valid = false;
        } else {
            mPhone.setError(null);
        }

        String city = mCity.getText().toString();
        if (TextUtils.isEmpty(city)) {
            mCity.setError("This field can not be empty.");
            valid = false;
        } else {
            mCity.setError(null);
        }

        return valid;
    }

}
