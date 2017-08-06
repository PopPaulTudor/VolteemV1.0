package com.volunteer.thc.volunteerapp.presentation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private ProgressDialog mProgressDialog;
    private Button mSendResetPasswordEmail;
    private EditText mResetPasswordEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            Log.w("AuthMethod", "Start activity");
            startActivityByClass(MainActivity.class);
        }

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    mProgressDialog = ProgressDialog.show(LoginActivity.this, "Logging in", "", true);
                    if (isNetworkAvailable()) {
                        logIn(mEmail.getText().toString(), mPassword.getText().toString());
                    } else {
                        mProgressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {
                    startActivityByClass(RegisterActivity.class);
                } else {
                    Toast.makeText(LoginActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.forgot_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(LoginActivity.this);
                View parentView = getLayoutInflater().inflate(R.layout.reset_password_bottom_sheet_design, null);
                mBottomSheetDialog.setContentView(parentView);
                BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from((View) parentView.getParent());
                mBottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension
                        (TypedValue.COMPLEX_UNIT_DIP, 200, getResources().getDisplayMetrics()));
                mBottomSheetDialog.show();

                mResetPasswordEmail = (EditText) parentView.findViewById(R.id.email_reset);
                mSendResetPasswordEmail = (Button) parentView.findViewById(R.id.send_email);

                mSendResetPasswordEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email = mResetPasswordEmail.getText().toString();
                        if (!TextUtils.isEmpty(email)) {
                            mResetPasswordEmail.setError(null);
                            mBottomSheetDialog.dismiss();

                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Password email sent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Reset failed. Verify if the email is written correctly and try again.", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
        });
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
                            ///TODO: handle more exceptions
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private boolean validateForm() {
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
