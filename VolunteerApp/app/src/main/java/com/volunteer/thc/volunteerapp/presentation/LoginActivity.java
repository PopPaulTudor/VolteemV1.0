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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.LoginEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.volunteer.thc.volunteerapp.R;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "EmailPassword";
    private static final int REQUEST_CODE = 1;
    private EditText mEmail, mPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private Button mSendResetPasswordEmail;
    private EditText mResetPasswordEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO on app release, remove debug from Fabric
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);

        setContentView(R.layout.activity_login);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccentDark));
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivityByClass(MainActivity.class);
        }

        mEmail = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mEmail.setFilters(new InputFilter[]{filter});

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
                            startActivityByClass(MainActivity.class);
                        } else {
                            Exception exception = task.getException();
                            if (exception != null) {
                                if (exception instanceof FirebaseAuthException) {
                                    if (exception.getMessage().contains("password")) {
                                        mPassword.setError(exception.getMessage());
                                        mPassword.requestFocus();
                                    } else if (exception.getMessage().contains("email") ||
                                            exception.getMessage().contains("account") ||
                                            exception.getMessage().contains("user")) {
                                        mEmail.setError(exception.getMessage());
                                        mEmail.requestFocus();
                                    } else {
                                        Log.e(TAG, exception.getMessage());
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
        // Log Crashlytics user
        logUser();
        Answers.getInstance().logLogin(new LoginEvent()
                .putMethod("LoginMethod")
                .putSuccess(true));

        Intent intent = new Intent(LoginActivity.this, activity);
        startActivity(intent);
        finish();
    }

    private void logUser() {
        if (mAuth.getCurrentUser() != null) {
            // You can call any combination of these three methods
            Crashlytics.setUserIdentifier(mAuth.getCurrentUser().getUid());
            Crashlytics.setUserEmail(mAuth.getCurrentUser().getEmail());
            Crashlytics.setUserName(mAuth.getCurrentUser().getDisplayName());
            Log.d(TAG, "Crashlytics setup successful!");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
