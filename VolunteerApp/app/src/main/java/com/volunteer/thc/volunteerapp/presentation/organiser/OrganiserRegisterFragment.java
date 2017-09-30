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
import android.text.Html;
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
    private Button mRegister, mBack;
    private Intent intent;
    private ProgressDialog mProgressDialog;
    private CircleImageView mImage;
    private Uri uri = null;
    private StorageReference mStorage;
    private boolean foundCode = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organiserregister, container, false);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mEmail = (EditText) view.findViewById(R.id.email);
        mConfirmPass = (EditText) view.findViewById(R.id.passwordConfirm);
        mPassword = (EditText) view.findViewById(R.id.password);
        mPhone = (EditText) view.findViewById(R.id.user_phone);
        mCity = (EditText) view.findViewById(R.id.user_city);
        mCompany = (EditText) view.findViewById(R.id.user_company);
        mRegister = (Button) view.findViewById(R.id.register_user);
        mImage = (CircleImageView) view.findViewById(R.id.photo);
        mRegisterCode = (EditText) view.findViewById(R.id.user_register_code);
        mBack = (Button) view.findViewById(R.id.back);
        intent = new Intent(getActivity(), MainActivity.class);

        uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.user)
                + '/' + getResources().getResourceTypeName(R.drawable.user) + '/' + getResources().getResourceEntryName(R.drawable.user));
        Picasso.with(getContext()).load(uri).fit().centerCrop().into(mImage);

        mRegister.setOnClickListener(new View.OnClickListener() {
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
                                if (TextUtils.equals(code, dataSnapshot1.getValue().toString())) {
                                    foundCode = true;
                                    createAccount(mEmail.getText().toString(), mPassword.getText().toString());
                                    mDatabase.child("codes/" + dataSnapshot1.getKey()).setValue(null);
                                    break;
                                }
                            }
                            if (!foundCode) {
                                mProgressDialog.dismiss();
                                mRegisterCode.setError("This code is invalid or has already been used.");
                                mRegisterCode.requestFocus();
                            } else {
                                mRegisterCode.setError(null);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (PermissionUtil.isStoragePermissionGranted(getContext())) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, GALLERY_INTENT);

                } else {
                    Snackbar.make(view, "Please allow storage permission", Snackbar.LENGTH_LONG).setAction("Set Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        }
                    }).show();
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            AlertDialog needCodeDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Registration code")
                    .setMessage("To avoid spam, please submit an email to contact.volteem@gmail.com to receive your organiser registration code.")
                    .setCancelable(false)
                    .setPositiveButton("I have a code", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
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
        Log.d("TAG", "CreateAccount: " + email);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserwithEmail:Succes");

                            FirebaseUser user = mAuth.getCurrentUser();

                            String userID = user.getUid();
                            String user_company = mCompany.getText().toString();
                            String user_city = mCity.getText().toString();
                            String user_phone = mPhone.getText().toString();

                            Organiser organiser = new Organiser(email, user_company, user_city, user_phone);

                            StorageReference filePath = mStorage.child("Photos").child("User").child(userID);
                            filePath.putBytes(ImageUtils.compressImage(uri, getActivity()));

                            mDatabase.child("users").child("organisers").child(userID).setValue(organiser);

                            UserProfileChangeRequest mProfileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(user_company)
                                    .build();

                            user.updateProfile(mProfileUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("ProfileUpdate ", "is successfull");
                                            } else {
                                                Log.d("ProfileUpdate ", "failed");
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
                                mEmail.setError("Email address is already in use.");
                                mEmail.requestFocus();
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
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
        valid = (editTextIsValid(mEmail) && editTextIsValid(mPassword) && editTextIsValid(mCompany) && editTextIsValid(mRegisterCode) &&
                editTextIsValid(mCity) && editTextIsValid(mPhone) && (uri != null) && checkPass());
        return valid;
    }

    private boolean editTextIsValid(EditText mEditText) {
        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            mEditText.setError("This field can not be empty.");
            mEditText.requestFocus();
            return false;
        } else {
            if (mEditText == mEmail && !text.contains("@") && !text.contains(".")) {
                mEditText.setError("Please enter a valid email address.");

            } else {
                if (mEditText == mPassword && text.length() < 6) {
                    mEditText.setError("Your password must be at least 6 characters long.");
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
            mConfirmPass.setError("Passwords do not match");
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
            Picasso.with(getContext()).load(uri).fit().centerCrop().into(mImage);


        }
    }
}

