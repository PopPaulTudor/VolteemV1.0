package com.volunteer.thc.volunteerapp.presentation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.model.Volunteer;

import static android.content.ContentValues.TAG;

/**
 * Created by Cristi on 6/18/2017.
 */

public class OrganiserRegisterFragment extends Fragment{

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private EditText mEmail, mPassword, mPhone, mCity, mCompany;
    private Button mRegister, mBack;
    private Intent intent, intent_back;
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserregister, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mEmail = (EditText) view.findViewById(R.id.email);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPhone = (EditText) view.findViewById(R.id.user_phone);
        mCity = (EditText) view.findViewById(R.id.user_city);
        mCompany = (EditText) view.findViewById(R.id.user_company);
        mRegister = (Button) view.findViewById(R.id.register_user);
        mBack = (Button) view.findViewById(R.id.back);
        intent = new Intent(getActivity(), MainActivity.class);
        intent_back = new Intent(getActivity(), LoginActivity.class);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateForm()) {
                    mProgressDialog = ProgressDialog.show(getActivity(), "Registering", "", true);
                }
                createAccount(mEmail.getText().toString(),mPassword.getText().toString());
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent_back);
                getActivity().finish();
            }
        });

        return view;
    }

    private void createAccount(final String email, String password) {

        Log.d("TAG", "CreateAccount: " + email);
        if(!validateForm()){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Log.d(TAG,"createUserwithEmail:Succes");

                            FirebaseUser user = mAuth.getCurrentUser();

                            String userID = user.getUid();
                            String user_company = mCompany.getText().toString();
                            String user_city = mCity.getText().toString();
                            String user_phone = mPhone.getText().toString();

                            Organiser organiser = new Organiser(email, user_company, user_city, user_phone);

                            mDatabase.child("users").child("organisers").child(userID).setValue(organiser);

                            UserProfileChangeRequest mProfileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(user_company)
                                    .build();
                            
                            user.updateProfile(mProfileUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Log.d("ProfileUpdate ", "is successfull");
                                            } else {
                                                Log.d("ProfileUpdate ", "failed");
                                            }
                                        }
                                    });

                            user.sendEmailVerification();
                            startActivity(intent);
                            getActivity().finish();

                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                mEmail.setError("Email address is already in use.");
                                mEmail.requestFocus();
                            } else {
                                if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    mEmail.setError("Please enter a valid email address.");
                                    mEmail.requestFocus();
                                } else {
                                    Log.w("Error registering ", task.getException());
                                }
                            }
                        }
                        mProgressDialog.dismiss();
                    }

                });

    }

    private boolean validateForm() {

        boolean valid;
        valid = (editTextIsValid(mEmail) && editTextIsValid(mPassword) && editTextIsValid(mCompany) &&
                        editTextIsValid(mCity) && editTextIsValid(mPhone));
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {
        String text = mEditText.getText().toString();
        if(TextUtils.isEmpty(text)) {
            mEditText.setError("This field can not be empty.");
            mEditText.requestFocus();
            return false;
        } else {
            if (mEditText == mPassword && text.length() < 6) {
                mEditText.setError("Your password must be at least 6 characters long.");
                mEditText.requestFocus();
                return false;
            } else {
                mEditText.setError(null);
            }
        }
        return true;
    }
}

