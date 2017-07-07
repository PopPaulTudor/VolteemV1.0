package com.volunteer.thc.volunteerapp.presentation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.volunteer.thc.volunteerapp.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    private EditText mEmail, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivityByClass(MainActivity.class);
                }
            }
        };

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    mProgressDialog = ProgressDialog.show(LoginActivity.this, "Logging in", "", true);
                }
                logIn(mEmail.getText().toString(), mPassword.getText().toString());
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityByClass(RegisterActivity.class);
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

    private void logIn(String email, String password) {
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
                        } else {
                            Exception exception = task.getException();
                            if (exception != null) {
                                if (exception instanceof FirebaseAuthException) {
                                    if (exception.getMessage().equals("The password is invalid or the user does not have a password.")) {
                                        mPassword.setError("Wrong password.");
                                        mPassword.requestFocus();
                                    } else if (exception.getMessage().equals("The user account has been disabled by an administrator.")) {
                                        mEmail.setError("Your account has been disabled by an administrator.");
                                        mEmail.requestFocus();
                                    }
                                } else {
                                    // In this case there can be any Exception
                                    Log.e(TAG, exception.getMessage());
                                }
                            }
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            //Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private boolean validateForm() {
        //TODO email check
        boolean valid = checkField(mEmail, "Please enter your email address.");
        valid &= checkField(mPassword, "Please enter your password.");
        return valid;
    }

    private boolean checkField(EditText editText, String errorMessage) {
        boolean result = true;
        String email = editText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            editText.setError(errorMessage);
            result = false;
        } else {
            editText.setError(null);
        }
        return result;
    }

    private void startActivityByClass(Class activity) {
        Intent intent = new Intent(LoginActivity.this, activity);
        startActivity(intent);
        finish();
    }
}
