package com.volunteer.thc.volunteerapp.presentation.organiser;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.SignUpEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.volunteer.thc.volunteerapp.R;
import com.volunteer.thc.volunteerapp.model.Organiser;
import com.volunteer.thc.volunteerapp.presentation.LoginActivity;
import com.volunteer.thc.volunteerapp.presentation.MainActivity;
import com.volunteer.thc.volunteerapp.util.ImageUtils;
import com.volunteer.thc.volunteerapp.util.PermissionUtil;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

/**
 * Created by Cristi on 6/18/2017.
 */

public class OrganiserRegisterFragment extends Fragment {

    private static final int GALLERY_INTENT = 1;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private EditText mEmail, mPassword, mPhone, mCity, mCompany, mConfirmPass, mRegisterCode;
    private Intent intent;
    private ProgressDialog mProgressDialog;
    private CircleImageView mImage;
    private Uri uri = null;
    private StorageReference mStorage;
    private boolean foundCode = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserregister, container, false);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mEmail = view.findViewById(R.id.email);
        mConfirmPass = view.findViewById(R.id.passwordConfirm);
        mPassword = view.findViewById(R.id.password);
        mPhone = view.findViewById(R.id.user_phone);
        mCity = view.findViewById(R.id.user_city);
        mCompany = view.findViewById(R.id.user_company);
        Button register = view.findViewById(R.id.register_user);
        mImage = view.findViewById(R.id.photo);
        mRegisterCode = view.findViewById(R.id.user_register_code);
        Button back = view.findViewById(R.id.back);
        intent = new Intent(getActivity(), MainActivity.class);

        uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.user)
                + '/' + getResources().getResourceTypeName(R.drawable.user) + '/' + getResources().getResourceEntryName(R.drawable.user));
        Picasso.get().load(uri).fit().centerCrop().into(mImage);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    mProgressDialog = ProgressDialog.show(getActivity(), "Registering", "", true);
                    final String code = mRegisterCode.getText().toString();
                    foundCode = false;
                    mDatabase.child("codes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (TextUtils.equals(code, String.valueOf(dataSnapshot1.getValue()))) {
                                    foundCode = true;
                                    createAccount(mEmail.getText().toString(), mPassword.getText().toString());
                                    //mDatabase.child("codes/" + dataSnapshot1.getKey()).setValue(null);
                                    break;
                                }
                            }
                            if (!foundCode) {
                                mProgressDialog.dismiss();
                                mRegisterCode.setError(getString(R.string.invalid_registration_code));
                                mRegisterCode.requestFocus();
                            } else {
                                mRegisterCode.setError(null);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // do nothing for now
                        }
                    });
                }
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (PermissionUtil.isStorageReadPermissionGranted(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);

                } else {
                    if (getActivity() != null) {
                        Snackbar.make(view, getString(R.string.storage_permission_needed), Snackbar.LENGTH_LONG).setAction("Set Permission", new View
                                .OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            }
                        }).show();
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LoginActivity.class));

                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null) {
            AlertDialog needCodeDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.registration_title))
                    .setMessage(getString(R.string.registration_code_message))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.code_have), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // do nothing for now
                        }
                    })
                    .setNegativeButton(getString(R.string.back), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(getActivity(), LoginActivity.class));
                            getActivity().finish();
                        }
                    })
                    .create();
            needCodeDialog.show();
        }
    }

    private void createAccount(final String email, String password) {
        Log.d(TAG, "CreateAccount: " + email);

        if (getActivity() != null) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail: Success");

                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user == null) {
                                    // TODO handle errors
                                    Log.w(TAG, "User not logged in!");
                                    return;
                                }

                                String userID = user.getUid();
                                String user_company = mCompany.getText().toString();
                                String user_city = mCity.getText().toString();
                                String user_phone = mPhone.getText().toString();

                                Organiser organiser = new Organiser(email, user_company, user_city, user_phone);

                                StorageReference filePath = mStorage.child("Photos").child("User").child(userID);
                                filePath.putBytes(ImageUtils.compressImage(uri, getActivity(), getResources()));

                                mDatabase.child("users").child("organisers").child(userID).setValue(organiser);

                                UserProfileChangeRequest mProfileUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(user_company)
                                        .build();

                                user.updateProfile(mProfileUpdate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "is successful");
                                                } else {
                                                    Log.d(TAG, "failed");
                                                }
                                            }
                                        });

                                Answers.getInstance().logSignUp(new SignUpEvent()
                                        .putMethod("Organiser")
                                        .putSuccess(true));

                                user.sendEmailVerification();
                                startActivity(intent);
                                getActivity().finish();
                            } else {
                                Answers.getInstance().logSignUp(new SignUpEvent()
                                        .putMethod("Organiser")
                                        .putSuccess(false));
                                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                    mEmail.setError(getString(R.string.existing_email_address));
                                    mEmail.requestFocus();
                                } else {
                                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                        mEmail.setError(getString(R.string.invalid_email_address));
                                        mEmail.requestFocus();
                                    } else {
                                        // TODO handle errors
                                        Log.w("Error registering ", task.getException());
                                    }
                                }
                            }
                            mProgressDialog.dismiss();
                        }

                    });
        }
    }

    private boolean validateForm() {
        boolean valid;
        valid = (editTextIsValid(mEmail) && editTextIsValid(mPassword) && editTextIsValid(mCompany) && editTextIsValid(mRegisterCode) &&
                editTextIsValid(mCity) && editTextIsValid(mPhone) && (uri != null) && checkPass());
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {
        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditText.setError(getString(R.string.field_empty));
            mEditText.requestFocus();
            return false;
        } else {
            if (mEditText == mEmail && !text.contains("@") && !text.contains(".")) {
                mEditText.setError(getString(R.string.invalid_email_address));
            } else {
                if (mEditText == mPassword && text.length() < 6) {
                    mEditText.setError(getString(R.string.invalid_password));
                    mEditText.requestFocus();
                    return false;
                } else {
                    mEditText.setError(null);
                }
            }
        }

        return true;
    }

    private boolean checkPass() {
        if (!mPassword.getText().toString().equals(mConfirmPass.getText().toString())) {
            mConfirmPass.setError(getString(R.string.password_not_match));
            mConfirmPass.requestFocus();
            return false;
        } else {
            mConfirmPass.setError(null);
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && (data != null)) {
            uri = data.getData();
            Picasso.get().load(uri).fit().centerCrop().into(mImage);
        }
    }
}

