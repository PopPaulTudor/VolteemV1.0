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
                            
                            user.updateProfile(mProfileUpdate);
                            user.sendEmailVerification();
                            Toast.makeText(getActivity(), "Account successfully created. A verification email has been sent to your email address.", Toast.LENGTH_LONG).show();

                            startActivity(intent);
                            getActivity().finish();

                        }else {
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthUserCollisionException e) {
                                mEmail.setError("Email address is already in use.");
                                mEmail.requestFocus();

                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mEmail.setError("Please enter a valid email address.");
                                mEmail.requestFocus();

                            } catch (Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            //Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                    }

                });

    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Please enter your email address.");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Please enter a password.");
            valid = false;
        } else {
            if(password.length() < 6) {
                mPassword.setError("Your password must be at least 6 characters long.");
            } else {
                mPassword.setError(null);
            }
        }

        String company = mCompany.getText().toString();
        if (TextUtils.isEmpty(company)) {
            mCompany.setError("Please enter your company's name.");
            valid = false;
        } else {
            mCompany.setError(null);
        }

        String city = mCity.getText().toString();
        if (TextUtils.isEmpty(city)) {
            mCity.setError("Please enter your city.");
            valid = false;
        } else {
            mCity.setError(null);
        }

        String phone = mPhone.getText().toString();
        if(TextUtils.isEmpty(phone)) {
            mPhone.setError("Please enter your phone number.");
        } else {
            mPhone.setError(null);
        }

        return valid;
    }

}

