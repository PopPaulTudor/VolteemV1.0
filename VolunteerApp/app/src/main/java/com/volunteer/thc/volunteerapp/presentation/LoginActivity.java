package com.volunteer.thc.volunteerapp.presentation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.volunteer.thc.volunteerapp.R;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mRegisterBtn,mLogInBtn;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String TAG = "EmailPassword";
    private Intent intent;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        intent = new Intent(this, MainActivity.class);
        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);
        mLogInBtn = (Button) findViewById(R.id.login);
        mRegisterBtn = (Button) findViewById(R.id.register);

        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null){

                    startActivity(intent);
                    finish();
                }

            }
        };

        mLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateForm()){
                    mProgressDialog = ProgressDialog.show(LoginActivity.this, "Logging in", "", true);
                }
                LogIn(mEmail.getText().toString(),mPassword.getText().toString());
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register_intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(register_intent);
                finish();
            }
        });
    }

    ///TODO email verif

    private void LogIn(String email, String password) {
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
                            startActivity(intent);
                            finish();

                        } else {

                            try {
                                throw task.getException();

                            } catch (FirebaseAuthException e){

                                if (e.getMessage().equals("The password is invalid or the user does not have a password.")){
                                    mPassword.setError("Wrong password.");
                                    mPassword.requestFocus();
                                } else{

                                    if(e.getMessage().equals("The user account has been disabled by an administrator.")){
                                        mEmail.setError("Your account has been disabled by an administrator.");
                                        mEmail.requestFocus();
                                    }
                                }

                            } catch (Exception e) {
                                Log.e(TAG,e.getMessage());
                            }
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
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
            mPassword.setError("Please enter your password.");
            valid = false;
        } else {
            mPassword.setError(null);
        }


        return valid;
    }
}
