package com.volunteer.thc.volunteerapp.presentation;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.User;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword, mCity, mFirstname, mLastname, mAge, mPhone;
    private Button mSignUpBtn,mSignInBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String TAG = "EmailPassword";
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = new Intent(this, MainActivity.class);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mSignInBtn = (Button) findViewById(R.id.email_sign_in_button);
        mSignUpBtn = (Button) findViewById(R.id.signUp);
        mAge = (EditText) findViewById(R.id.user_age);
        mFirstname = (EditText) findViewById(R.id.first_name);
        mLastname = (EditText) findViewById(R.id.last_name);
        mCity = (EditText) findViewById(R.id.user_city);
        mPhone = (EditText) findViewById(R.id.user_phone);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){

                    startActivity(intent);
                }

            }
        };

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn(mEmail.getText().toString(),mPassword.getText().toString());
            }
        });

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPhone.getVisibility() == View.VISIBLE) {
                    createAccount(mEmail.getText().toString(), mPassword.getText().toString());

                    mPhone.setVisibility(View.GONE);
                    mFirstname.setVisibility(View.GONE);
                    mLastname.setVisibility(View.GONE);
                    mCity.setVisibility(View.GONE);
                    mAge.setVisibility(View.GONE);
                    mSignInBtn.setVisibility(View.VISIBLE);
                } else{

                    mPhone.setVisibility(View.VISIBLE);
                    mFirstname.setVisibility(View.VISIBLE);
                    mLastname.setVisibility(View.VISIBLE);
                    mCity.setVisibility(View.VISIBLE);
                    mAge.setVisibility(View.VISIBLE);
                    mSignInBtn.setVisibility(View.GONE);
                }
            }
        });

    }

    @Override
    public void onBackPressed(){

        if(mPhone.getVisibility() == View.VISIBLE){

            mPhone.setVisibility(View.GONE);
            mFirstname.setVisibility(View.GONE);
            mLastname.setVisibility(View.GONE);
            mCity.setVisibility(View.GONE);
            mAge.setVisibility(View.GONE);
            mSignInBtn.setVisibility(View.VISIBLE);
            
        } else {
            super.onBackPressed();
        }
    }

    ///TODO email verif

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    private void createAccount(final String email, String password) {

        Log.d(TAG, "CreateAccount: " + email);
        if(!validateForm()){
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            Log.d(TAG,"createUserwithEmail:Succes");
                            FirebaseUser user = mAuth.getCurrentUser();

                            String userID = user.getUid();
                            String user_firstname = mFirstname.getText().toString();
                            String user_lastname = mLastname.getText().toString();
                            String user_age = mAge.getText().toString();
                            String user_city = mCity.getText().toString();
                            String user_phone = mPhone.getText().toString();

                            User user1 = new User(user_firstname,user_lastname,email,user_age,user_city,user_phone);

                            mDatabase.child("users").child(userID).setValue(user1);

                            startActivity(intent);

                        }else{
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }


    private boolean validateForm() {
        boolean valid = true;

        String email = mEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Please enter an email address."); ///TODO check email format
            valid = false;
        } else {
            mEmail.setError(null);
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Please enter a password"); ///TODO check password length
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }
}
